package org.crawler.repo;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.crawler.FetchUrl;
import org.crawler.config.Configuration;

/**
 *
 */
public class DepthQueueRepository {

    private Queue<FetchUrl> currentDepthQueue;
    private Queue<FetchUrl> nextDepthQueue;
    private final Set<Integer> crawledUrls;

    private final Object lock = new Object();

    public DepthQueueRepository(Queue<FetchUrl> currentDepthQueue, Queue<FetchUrl> nextDepthQueue) {
        this.currentDepthQueue = currentDepthQueue;
        this.nextDepthQueue = nextDepthQueue;
        this.crawledUrls = new HashSet<>();
    }

    public boolean insert(FetchUrl furl) {
        if (crawledUrls.contains(furl.hashCode())) {
            return false;
        }
        synchronized (lock) {
            if (nextDepthQueue.offer(furl)) {
                crawledUrls.add(furl.hashCode());
                return true;
            }
        }
        return false;
    }

    public FetchUrl getNext() {
        FetchUrl furl = currentDepthQueue.poll();
        if (furl != null) {
            return furl;
        }
        synchronized (lock) {
            while (getSize() == 0) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DepthQueueRepository.class.getName()).log(Level.ERROR, null, ex);
                    return null;
                }
            }
            return currentDepthQueue.poll();
        }
    }

    public void goToNextDepth() {
        synchronized (lock) {
            Logger l = Configuration.getLogger(DepthQueueRepository.class.getName());
            l.info("going to the next dept, urls to crawl:   " + nextDepthQueue.size() + "    total amount:  " + crawledUrls.size());
            currentDepthQueue.clear();
            Queue<FetchUrl> temp = currentDepthQueue;
            currentDepthQueue = nextDepthQueue;
            nextDepthQueue = temp;
            nextDepthQueue.clear();
            lock.notifyAll();
        }
    }

    public void clear() {
        currentDepthQueue.clear();
        nextDepthQueue.clear();
        crawledUrls.clear();
    }

    public int getSize() {
        return currentDepthQueue.size();
    }

    public boolean isEmpty() {
        return currentDepthQueue.size() == 0 && nextDepthQueue.size() == 0;
    }

}
