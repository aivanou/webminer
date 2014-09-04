package org.crawler;

import java.util.Objects;
import org.crawler.fetch.ProtocolType;

/**
 *
 * Represents the domain of the site
 * 
*/
public class Domain implements Comparable<Domain> {

    public enum DomainStatus {

        NotCrawled,
        Crawling,
        Crawled;
    };

    private ProtocolType protocol;
    private String domain;
    private String startUrl;
    private long lastCrawledDate = 0;
    private long crawlPeriod = -1;
    private long creationDate;
    private long nextCrawlTime;
    private int maxDepth;
    private DomainMetadata metadata;
    private DomainStatus dstatus;

    public static class DomainBuilder {

        private DomainMetadata dm;
        private long lastCrawledDate;
        private long crawlPeriod;
        private long creationDate;
        private long nextCrawlTime;
        private int maxDepth;

        public DomainBuilder() {
            this.dm = new DomainMetadata();
            this.crawlPeriod = -1;
            this.creationDate = System.currentTimeMillis();
            this.lastCrawledDate = 0;
            this.nextCrawlTime = Long.MAX_VALUE;
        }

        public DomainBuilder setDomainMetadata(DomainMetadata dm) {
            this.dm = dm;
            return this;
        }

        public DomainBuilder setCreationDate(long cd) {
            this.creationDate = cd;
            return this;
        }

        public DomainBuilder setCrawlPeriod(long cp) {
            this.crawlPeriod = cp;
            return this;
        }

        public DomainBuilder setLastCrawledDate(long lcd) {
            this.lastCrawledDate = lcd;
            return this;
        }

        public DomainBuilder setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Domain build(String url) {
            setNextCrawlTime();
            return new Domain(url, dm, creationDate, crawlPeriod, lastCrawledDate, nextCrawlTime, maxDepth);
        }

        public Domain build(ProtocolType pt, String domain) {
            setNextCrawlTime();
            return new Domain(pt, domain, dm, creationDate, crawlPeriod, lastCrawledDate, nextCrawlTime, maxDepth);
        }

        private void setNextCrawlTime() {
            if (crawlPeriod != -1) {
                if (lastCrawledDate != 0) {
                    nextCrawlTime = lastCrawledDate + crawlPeriod;
                } else {
                    nextCrawlTime = creationDate + crawlPeriod;
                }
            } else {
                nextCrawlTime = Long.MAX_VALUE;
            }
        }

    }

    private Domain(ProtocolType pt, String domain, DomainMetadata dm, long creationDate, long crawlPeriod, long lastCrawledDate, long nextCrawlTime, int maxDepth) {
        this(dm, creationDate, crawlPeriod, lastCrawledDate, nextCrawlTime, maxDepth);
        this.protocol = pt;
        this.domain = domain;
        this.startUrl = pt.toString() + "://" + domain;
    }

    private Domain(String url, DomainMetadata dm, long creationDate, long crawlPeriod, long lastCrawledDate, long nextCrawlTime, int maxDepth) {
        this(dm, creationDate, crawlPeriod, lastCrawledDate, nextCrawlTime, maxDepth);
        this.startUrl = url;
        this.protocol = HttpUtil.getProtocol(url);
        this.domain = HttpUtil.getDomain(url);

    }

    private Domain(DomainMetadata dm, long creationDate, long crawlPeriod, long lastCrawledDate, long nextCrawlTime, int maxDepth) {
        this.creationDate = creationDate;
        this.crawlPeriod = crawlPeriod;
        this.lastCrawledDate = lastCrawledDate;
        this.metadata = dm;
        this.nextCrawlTime = nextCrawlTime;
        this.maxDepth = maxDepth;
    }

    public void setMaxDepth(int mdepth) {
        this.maxDepth = mdepth;
    }

    public void changeNextCrawlTime() {
        this.nextCrawlTime = this.lastCrawledDate + this.crawlPeriod;
    }

    public void setLastCrawledDate(long lastCrawledDate) {
        this.lastCrawledDate = lastCrawledDate;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public long getNextCrawlTime() {
        return nextCrawlTime;
    }

    public DomainMetadata getMetadata() {
        return metadata;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isPageCrawled(String url) {
        return metadata.isPageCrawled(url);
    }

    public long getLastCrawledDate() {
        return lastCrawledDate;
    }

    public long getCrawlPeriod() {
        return crawlPeriod;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public DomainStatus getDstatus() {
        return dstatus;
    }

    public synchronized void changeDomainStatus(DomainStatus status) {
        this.dstatus = status;
    }

    public synchronized DomainStatus getStatus() {
        return this.dstatus;
    }

    public void clear() {
        metadata.clear();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.protocol);
        hash = 73 * hash + Objects.hashCode(this.domain);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Domain other = (Domain) obj;
        if (this.protocol != other.protocol) {
            return false;
        }
        return Objects.equals(this.domain, other.domain);
    }

    @Override
    public String toString() {
        return protocol.toString() + "://" + domain.toLowerCase();
    }

    @Override
    public int compareTo(Domain o) {
        if (o == null) {
            return -1;
        }
        return toString().compareTo(o.toString());
    }

}
