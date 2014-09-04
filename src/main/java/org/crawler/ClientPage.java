package org.crawler;

import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class ClientPage {

    private String domain;
    private String url;
    private String clientId;
    private String uniqueId;
    private Map<String, String> data;

    public ClientPage(String domain, String url, String clientId, String uniqueId, Map<String, String> data) {
        this.domain = domain;
        this.url = url;
        this.clientId = clientId;
        this.uniqueId = uniqueId;
        this.data = data;
    }

    public String getDomain() {
        return domain;
    }

    public String getUrl() {
        return url;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Map<String, String> getData() {
        return data;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.domain);
        hash = 41 * hash + Objects.hashCode(this.url);
        hash = 41 * hash + Objects.hashCode(this.clientId);
        hash = 41 * hash + Objects.hashCode(this.uniqueId);
        hash = 41 * hash + Objects.hashCode(this.data);
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
        final ClientPage other = (ClientPage) obj;
        if (!Objects.equals(this.domain, other.domain)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.clientId, other.clientId)) {
            return false;
        }
        if (!Objects.equals(this.uniqueId, other.uniqueId)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

}
