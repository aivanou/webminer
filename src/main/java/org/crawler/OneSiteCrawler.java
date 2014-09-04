package org.crawler;

import com.mongodb.MongoClient;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.crawler.config.Configuration;
import org.crawler.repo.BlockingMemoryRepository;
import org.crawler.repo.ClientDomainRepository;
import org.crawler.repo.QueueRepository;
import org.crawler.repo.mongodb.DomainDataLayer;

/**
 *
 */
public class OneSiteCrawler {

    public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException, InterruptedException {
        List<Integer> l = new ArrayList<>(10);
        l.add(1);
        l.add(1);
        l.add(1);
        l.add(1);
        l.add(1);
        System.out.println(l.size());
        l.clear();
        System.out.println(l.size());
    }
}
