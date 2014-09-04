package org.crawler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DomainMetadata {

    private final Set<Integer> crawledUrls;
    private final AtomicInteger crawledUrlsAmount;
    private final AtomicInteger busyUrls;
    private final AtomicInteger depth;

    public DomainMetadata() {
        this.crawledUrls = java.util.Collections.synchronizedSet(new HashSet<Integer>());
        this.busyUrls = new AtomicInteger(0);
        this.depth = new AtomicInteger(0);
        this.crawledUrlsAmount = new AtomicInteger(0);
    }

    public void clear() {
        crawledUrls.clear();
    }

    public void incDepth() {
        depth.incrementAndGet();
    }

    public int getDepth() {
        return depth.get();
    }

    public void acquireUrl() {
        busyUrls.incrementAndGet();
    }

    public void releaseUrl() {
        busyUrls.decrementAndGet();
    }

    public int getBusyUrlsAmount() {
        return busyUrls.get();
    }

    public boolean isPageCrawled(String pageUrl) {
        return crawledUrls.contains(pageUrl.hashCode());
    }

    public void crawlPage(String pageUrl) {
        if (crawledUrls.add(pageUrl.hashCode())) {
            crawledUrlsAmount.incrementAndGet();
        }
    }

    public int getCrawledUrlsSize() {
        return crawledUrlsAmount.get();
    }

}
