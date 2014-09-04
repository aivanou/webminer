package org.crawler.repo;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.crawler.ClientDomain;
import org.crawler.config.Configuration;
import org.crawler.repo.mongodb.DomainDataLayer;

/**
 *
 */
public class ClientDomainRepository {

    private final DomainDataLayer domainStorage;

    private final Queue<ClientDomain> cache;
    private final int cacheSize;
    private final int period = 5 * 1000;
    private final Logger logger = Configuration.getLogger("domain repository");

    public ClientDomainRepository(DomainDataLayer domainStorage, Queue<ClientDomain> cacheQueue, int cacheSize) {
        this.domainStorage = domainStorage;
        this.cache = cacheQueue;
        this.cacheSize = cacheSize;
    }

    public ClientDomain provide() throws InterruptedException {
        ClientDomain cdomain = cache.poll();
        if (cdomain != null) {
            return cdomain;
        }
        while (true) {
            if (tryFillCache()) {
                ClientDomain cd = cache.poll();
                return cd;
            }
            Thread.sleep(period);
        }
    }

    public ClientDomain provide(long timeout, TimeUnit tunit) throws InterruptedException {
        ClientDomain cdomain = cache.poll();
        if (cdomain != null) {
            return cdomain;
        }
        long interruptTime = System.currentTimeMillis() + tunit.toMillis(timeout);
        while (true) {
            if (tryFillCache()) {
                return cache.poll();
            }
            Thread.sleep(period);
            if (interruptTime <= System.currentTimeMillis()) {
                throw new InterruptedException();
            }
        }
    }

    public void rewindCachedDomains() {
        for (ClientDomain cdomain : cache) {
            rewind(cdomain);
        }
    }

    public void rewind(ClientDomain cdomain) {
        cdomain.setIsBusy(false);
        if (cdomain.getDomain().getCrawlPeriod() == -1) {
            cdomain.setNewDomain(true);
        }
        cdomain.getDomain().setLastCrawledDate(cdomain.getDomain().getLastCrawledDate() - cdomain.getDomain().getCrawlPeriod());
        cdomain.getDomain().changeNextCrawlTime();
        domainStorage.insertOrUpdate(cdomain);
    }

    public void releaseCachedDomains() {
        for (ClientDomain cdomain : cache) {
            release(cdomain);
        }
    }

    public void release(ClientDomain cdomain) {
        cdomain.getDomain().setLastCrawledDate(System.currentTimeMillis());
        cdomain.setIsBusy(false);
        cdomain.getDomain().changeNextCrawlTime();
        domainStorage.insertOrUpdate(cdomain);
    }

    private boolean tryFillCache() {
        List<ClientDomain> domainsToCrawl = domainStorage.getDomainsToCrawl(cacheSize, System.currentTimeMillis());
        logger.info("got from database:  " + domainsToCrawl.size());
        if (domainsToCrawl.isEmpty()) {
            return false;
        }
        for (ClientDomain cdomain : domainsToCrawl) {
            cache.add(cdomain);
            cdomain.setIsBusy(true);
            cdomain.setNewDomain(false);
            domainStorage.insertOrUpdate(cdomain);
        }
        return true;
    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        MongoClient mclient = new MongoClient("localhost", 27017);
        DomainDataLayer dl = new DomainDataLayer(mclient, "cdomainTest", "domains", 100);
        final ClientDomainRepository crepo = new ClientDomainRepository(dl, new ConcurrentLinkedQueue<ClientDomain>(), 20);
        crepo.provide(10, TimeUnit.SECONDS);
//        Runnable r = new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    ClientDomain cdomain = crepo.provide();
//                    System.out.println(cdomain.getDomain());
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(ClientDomainRepository.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//            }
//        };
//        Thread t = new Thread(r);
//        t.start();
//        Thread.sleep(5000);
//        t.interrupt();
    }

}
//TODO: Write tests
//TODO: Write release domain
