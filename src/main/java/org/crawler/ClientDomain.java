package org.crawler;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.crawler.Domain;

/**
 *
 */
public class ClientDomain {

    private Domain domain;
    private String uniqueId;
    private String name;
    private String type;
    private boolean newDomain;
    private boolean busy;
    private long startCrawlingTime;
    private Map<String, PatternStorage> clientsPatterns;
    private int urlsVisited;
    private int products;

    public ClientDomain(Domain domain, String uniqueId, String name, String type, boolean newDomain, Map<String, PatternStorage> patterns) {
        this.domain = domain;
        this.uniqueId = uniqueId;
        this.name = name;
        this.type = type;
        this.newDomain = newDomain;
        this.busy = false;
        this.clientsPatterns = patterns;
        this.urlsVisited = 0;
        this.products = 0;
    }

    public long getStartCrawlingTime() {
        return startCrawlingTime;
    }

    public void setStartCrawlingTime(long startCrawlingTime) {
        this.startCrawlingTime = startCrawlingTime;
    }

    public void incUrlsVisited() {
        this.urlsVisited += 1;
    }

    public int getUrlsVisited() {
        return urlsVisited;
    }

    public void setUrlsVisited(int urlsVisited) {
        this.urlsVisited = urlsVisited;
    }

    public void incProducts() {
        this.products += 1;
    }

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public Map<String, PatternStorage> getAllPatterns() {
        return clientsPatterns;
    }

    public PatternStorage getPatternsById(String id) {
        return clientsPatterns.get(id);
    }

    public boolean isBusy() {
        return busy;
    }

    public void setNewDomain(boolean newDomain) {
        this.newDomain = newDomain;
    }

    public void setIsBusy(boolean isBusy) {
        this.busy = isBusy;
    }

    public boolean isNewDomain() {
        return newDomain;
    }

    public Domain getDomain() {
        return domain;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.domain);
        hash = 29 * hash + Objects.hashCode(this.uniqueId);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.type);
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
        final ClientDomain other = (ClientDomain) obj;
        if (!Objects.equals(this.domain, other.domain)) {
            return false;
        }
        if (!Objects.equals(this.uniqueId, other.uniqueId)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }

}
