package org.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 *
 */
public class FetchUrl {

    private final PageMetadata pageMetadata;
    private final URL processUrl;
    private final ClientDomain domain;

    public FetchUrl(URL url, ClientDomain head, PageMetadata pageMetadata) {
        this.pageMetadata = pageMetadata;
        this.domain = head;
        this.processUrl = url;
    }

    public FetchUrl(String url, ClientDomain head, PageMetadata pageMetadata) throws MalformedURLException {
        this.pageMetadata = pageMetadata;
        this.processUrl = new URL(url);
        this.domain = head;
    }

    public PageMetadata getPageMetadata() {
        return pageMetadata;
    }

    public URL getProcessUrl() {
        return processUrl;
    }

    public ClientDomain getClientDomain() {
        return domain;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.processUrl);
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
        final FetchUrl other = (FetchUrl) obj;
        if (!Objects.equals(this.processUrl, other.processUrl)) {
            return false;
        }
        return true;
    }

}
