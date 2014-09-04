package org.crawler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.crawler.config.Configuration;
import org.crawler.fetch.HttpFetcher;
import org.crawler.fetch.ProtocolException;
import org.crawler.fetch.ProtocolFetcher;
import org.crawler.fetch.ProtocolResponse;
import org.crawler.fetch.ProtocolStatus;
import org.crawler.parse.HtmlParser;
import org.crawler.parse.ParseOutput;
import org.crawler.repo.ClientDomainRepository;
import org.crawler.repo.DepthQueueRepository;
import org.crawler.repo.mongodb.DomainDataLayer;
import org.crawler.repo.mongodb.PageDataLayer;

public class Crawler implements Runnable {

    private final List<DepthQueueRepository> concurrentQueues;
    private final List<ClientDomain> workingDomains;
    private final ClientDomainRepository domainRepository;
    private final PageDataLayer pageDataLayer;
    private final int id;
    private final int concurrentDomainsSize;

    private final Logger logger;

    private static Object[] locks;

    //change to dependency injection
    private static ProtocolFetcher fetcher = HttpFetcher.init();
    private static HtmlParser parser = new HtmlParser();

    public static void configure(int concurrentDomains) {
        locks = new Object[concurrentDomains];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
    }

    public Crawler(int id, List<DepthQueueRepository> concurrentQueues,
            ClientDomainRepository domainRepository, int concurrentDomains,
            PageDataLayer pageDataLayer, List<ClientDomain> cdomains, Logger logger) {
        this.concurrentQueues = concurrentQueues;
        this.domainRepository = domainRepository;
        this.pageDataLayer = pageDataLayer;
        this.concurrentDomainsSize = concurrentDomains;
        this.id = id;
        this.logger = logger;
        this.workingDomains = new ArrayList<>(cdomains);
    }

    private FetchUrl getNext() {
        int index = id % concurrentDomainsSize;
        DepthQueueRepository queue;
        synchronized (locks[index]) {
            queue = concurrentQueues.get(index);
        }
        FetchUrl furl = queue.getNext();
        return furl;
    }

    private boolean changeDomain(ClientDomain cdomain, PageMetadata pm) {
        int index = id % concurrentDomainsSize;
        synchronized (locks[index]) {
            try {
                if (cdomain.getDomain().getStatus() == Domain.DomainStatus.Crawled) {
                    return false;
                }
                logger.info("releasing: " + cdomain.getDomain().toString());
                logger.info("finished crawling " + cdomain.getDomain() + " started:  " + new Date(cdomain.getStartCrawlingTime())
                        + "; finished: " + new Date(System.currentTimeMillis()) + "  urls visited:  " + cdomain.getUrlsVisited() + "; products:  " + cdomain.getProducts());
                cdomain.getDomain().changeDomainStatus(Domain.DomainStatus.Crawled);
                DepthQueueRepository currentQueue = concurrentQueues.get(index);
                currentQueue.clear();
                domainRepository.release(cdomain);
                workingDomains.remove(cdomain);
                cdomain.getDomain().clear();
                ClientDomain clientDomainToCrawl = domainRepository.provide();
                try {
                    currentQueue.insert(new FetchUrl(clientDomainToCrawl.getDomain().toString(), clientDomainToCrawl, new PageMetadata(0)));
                    currentQueue.goToNextDepth();
                    workingDomains.add(cdomain);
                } catch (MalformedURLException ignore) {
                    return false;
                }
                return true;
            } catch (InterruptedException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.ERROR, null, ex);
            }
        }
        return true;
    }

    private boolean add(URL url, ClientDomain domain, PageMetadata pmetadata) {
        int index = id % concurrentDomainsSize;
        DepthQueueRepository queue;
        synchronized (locks[index]) {
            queue = concurrentQueues.get(index);
        }
        if (!domain.getDomain().isPageCrawled(url.toString())) {
            return queue.insert(new FetchUrl(url, domain, pmetadata));
        }
        return false;
    }

    private boolean isDepthFinished(FetchUrl furl, DepthQueueRepository queue) {
        return furl.getClientDomain().getDomain().getMetadata().getBusyUrlsAmount() == 0
                && queue.getSize() == 0;
    }

    @Override
    public void run() {
        while (true) {
            FetchUrl furl = getNext();//null can be thrown
            if (furl == null) {
                continue;
            }
            if (furl.getClientDomain().getDomain().getStatus() == Domain.DomainStatus.Crawled) {
                continue;
            }
            furl.getClientDomain().incUrlsVisited();
//            logger.info("crawling:  " + furl.getProcessUrl().toString());
            furl.getClientDomain().getDomain().getMetadata().acquireUrl();
            ProtocolResponse fetchOutput;
            try {
                fetchOutput = fetcher.fetch(furl.getProcessUrl().toString());
            } catch (ProtocolException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.ERROR, null, ex);
                post_process(furl);
                continue;
            }
            if (fetchOutput.getStatusState().getState() != ProtocolStatus.Type.Valid) {
                post_process(furl);
                continue;
            }
            ParseOutput out = parser.parse(fetchOutput.getProtocolOutput(), furl.getClientDomain().getAllPatterns());
            if (validate(out)) {
                furl.getClientDomain().incProducts();
                Collection<ClientPage> cpage = convert(out, furl.getClientDomain(), furl.getProcessUrl().toString());
                pageDataLayer.delayedInsertOrUpdate(cpage);
            }
            for (URL url : out.getLinks()) {
                add(url, furl.getClientDomain(), new PageMetadata(furl.getPageMetadata().getDepth() + 1));
            }
            post_process(furl);
        }
    }

    private Collection<ClientPage> convert(ParseOutput pout, ClientDomain cdomain, String url) {
        Map<String, Map<String, String>> clientsData = pout.getOutput();
        Collection<ClientPage> cpages = new ArrayList<>();
        for (String clientId : clientsData.keySet()) {
            ClientPage cpage = convert(cdomain, clientId, url, clientsData.get(clientId));
            if (cpage != null) {
                cpages.add(cpage);
            }
        }
        return cpages;
    }

    private ClientPage convert(ClientDomain cdomain, String clientId, String url, Map<String, String> data) {
        if (data.isEmpty()) {
            return null;
        }
        ClientPage cpage = new ClientPage(cdomain.getDomain().toString(), url, clientId,
                cdomain.getUniqueId() + Integer.toString(url.hashCode()), data);
        return cpage;
    }

    private boolean validate(ParseOutput pout) {
        for (String obj : pout.getOutput().keySet()) {
            if (pout.getOutput().get(obj).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void post_process(FetchUrl furl) {
        int index = id % concurrentDomainsSize;
        DepthQueueRepository queue;
        synchronized (locks[index]) {
            queue = concurrentQueues.get(index);
        }

        furl.getClientDomain().getDomain().getMetadata().releaseUrl();
        if (isDepthFinished(furl, queue)) {
            System.out.println("domain:  " + furl.getClientDomain().getDomain().toString() + "  ;  depth " + furl.getPageMetadata().getDepth() + "  finished; status:  " + queue.isEmpty());
            if (queue.isEmpty()
                    || furl.getPageMetadata().getDepth() >= furl.getClientDomain().getDomain().getMaxDepth()) {
                if (furl.getClientDomain().getDomain().getStatus() != Domain.DomainStatus.Crawled) {
                    changeDomain(furl.getClientDomain(), furl.getPageMetadata());
                }
            } else {
                queue.goToNextDepth();
            }
        }
    }

    public static void insert(String filename, String propertyFile) throws IOException, URISyntaxException {
        Properties props = new Properties();
        System.out.println(filename);
        System.out.println(propertyFile);
        InputStream stream = new FileInputStream(propertyFile);
        props.load(stream);
        String database = props.getProperty("mongodb.database");
        List<ClientDomain> domainsToCrawl = Configuration.readDomains(new File(filename));
        MongoClient mclient = new MongoClient(props.getProperty("mongodb.host"), Integer.parseInt(props.getProperty("mongodb.port").toString()));
        DomainDataLayer dl = new DomainDataLayer(mclient, database, props.getProperty("mongodb.domaincollection"), 10);
        for (ClientDomain domain : domainsToCrawl) {
            dl.insertOrUpdate(domain);
        }
        mclient.close();
    }

    public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException, InterruptedException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("log4j.properties");
        PropertyConfigurator.configure(is);
        Logger logger = Configuration.getLogger(Crawler.class.getName());
        if (args.length > 0 && args[0].equals("insert")) {
            String dFile = "domains.txt";
            String pFile = "webcrawler.properties";

            if (args.length >= 3) {
                dFile = args[1];
                pFile = args[2];
            } else {
                dFile = Configuration.getPropertyIfNotNull("dFile", dFile);
                pFile = Configuration.getPropertyIfNotNull("pFile", pFile);
            }
            insert(dFile, pFile);
            return;
        }

        String pFile = "webcrawler.properties";

//        if (!args[0].equals("crawl")) {
//            return;
//        }
        if (args.length > 0 && args.length >= 1) {
            pFile = args[1];
        } else {
            pFile = Configuration.getPropertyIfNotNull("pfile", pFile);
        }
        Properties props = new Properties();
        InputStream stream = new FileInputStream(pFile);
        props.load(stream);

        int threads = Integer.parseInt(props.getProperty("domains.threads").toString());
        int maxConcurrentDomains = Integer.parseInt(props.get("domains.concurrent").toString());

        String msg = String.format("Starting crawler with next properties: \n number of threads: %d \n concurrent domains: %d \n database host: %s:%d  ",
                threads, maxConcurrentDomains, props.getProperty("mongodb.host").toString(), Integer.parseInt(props.getProperty("mongodb.port").toString()));
        logger.info(msg);

        List<ClientDomain> cdomains = new ArrayList<>();
        String database = props.getProperty("mongodb.database");
        Crawler.configure(maxConcurrentDomains);
        List<DepthQueueRepository> concurrentQueues = new java.util.concurrent.CopyOnWriteArrayList<>();
        MongoClient mclient = new MongoClient(props.getProperty("mongodb.host"), Integer.parseInt(props.getProperty("mongodb.port").toString()));
        DomainDataLayer dl = new DomainDataLayer(mclient, database, props.getProperty("mongodb.domaincollection"), 10);
        ClientDomainRepository clientDomainRepo = new ClientDomainRepository(dl, new ConcurrentLinkedQueue<ClientDomain>(), 100);
        for (int i = 0; i < maxConcurrentDomains; i++) {
            Queue<FetchUrl> cdepthQueue = new ArrayBlockingQueue<>(Integer.parseInt(props.getProperty("domains.queuesize").toString()));
            Queue<FetchUrl> ndepthQueue = new ArrayBlockingQueue<>(Integer.parseInt(props.getProperty("domains.queuesize").toString()));
            DepthQueueRepository queue = new DepthQueueRepository(cdepthQueue, ndepthQueue);

            ClientDomain cdomain = clientDomainRepo.provide();
            cdomain.setStartCrawlingTime(System.currentTimeMillis());
            logger.info("Starting to crawl: " + cdomain.getDomain().toString() + "  ; Date:  " + new Date(System.currentTimeMillis()));
            queue.insert(new FetchUrl(cdomain.getDomain().toString(), cdomain, new PageMetadata(cdomain.getDomain().getMetadata().getDepth())));
            queue.goToNextDepth();
            concurrentQueues.add(queue);
            cdomains.add(cdomain);
        }
        PageDataLayer pdal = new PageDataLayer(mclient, database, props.getProperty("mongodb.pagecollection").toString(), 100);
        ExecutorService exec = Executors.newCachedThreadPool();

        for (int i = 0; i < threads; i++) {
            exec.execute(new Crawler(i, concurrentQueues, clientDomainRepo, maxConcurrentDomains, pdal, cdomains, logger));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(clientDomainRepo, cdomains, concurrentQueues)));
    }

    public static void printHelp() {
        System.out.println("help");
        System.out.println("In order to run crawler, specify first argument as: crawl ");
        System.out.println("specify the property file please, use one of two solutions:");
        System.out.println("1. Specify as a property: -DpFile=filepath");
        System.out.println("2. Specify as a parameter: crawl property_file_path");
        System.out.println("In order to run insert, specify the first argument: insert");
    }

    private static class ShutdownHook implements Runnable {

        private final Logger logger = Configuration.getLogger("shutdown");

        private final List<ClientDomain> workingDomains;
        private final List<DepthQueueRepository> concurrentQueues;
        private final ClientDomainRepository domainRepository;

        public ShutdownHook(ClientDomainRepository domainRepository, List<ClientDomain> cdomains, List<DepthQueueRepository> cqueues) {
            this.domainRepository = domainRepository;
            this.workingDomains = cdomains;
            this.concurrentQueues = cqueues;
        }

        @Override
        public void run() {
            logger.info("Releasing domains: ");
            for (ClientDomain cdomain : workingDomains) {
                domainRepository.rewind(cdomain);
                logger.info(cdomain.getDomain().toString());
            }
            domainRepository.rewindCachedDomains();
            for (DepthQueueRepository dqueue : concurrentQueues) {
                dqueue.clear();
            }
        }
    }

}
