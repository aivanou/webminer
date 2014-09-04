package org.crawler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.crawler.fetch.ProtocolType;

/**
 *
 */
public final class HttpUtil {

    public static ProtocolType getProtocol(String url) {
        url = url.trim();
        if (url.isEmpty()) {
            return ProtocolType.Unknown;
        }
        int ind = url.indexOf("://");
        if (ind == -1) {
            return ProtocolType.Unknown;
        }
        ProtocolType pt = ProtocolType.getType(url.substring(0, ind));
        if (pt == null) {
            return ProtocolType.Unknown;
        }
        return pt;
    }

    public static String getDomain(String url) {
        url = url.trim();
        if (url.isEmpty()) {
            return "";
        }
        String domain;
        int ind = url.indexOf("://");
        if (ind != -1) {
            domain = url.substring(ind + 3);
        } else {
            domain = url;
        }
        ind = domain.indexOf("/");
        if (ind != -1) {
            return domain.substring(0, ind);
        }
        ind = domain.indexOf("#");
        if (ind != -1) {
            return domain.substring(0, ind);
        }
        return domain;
    }

    public static String normalizeUrl(String urlFrom, String newPath) {
        if (urlFrom.endsWith("/")) {
            urlFrom = urlFrom.substring(0, urlFrom.length() - 1);
        }
        if (newPath.contains("#")) {
            newPath = newPath.substring(0, newPath.indexOf("#"));
        }
        if (newPath.startsWith("//")) {
            return HttpUtil.getProtocol(urlFrom).toString() + ":" + newPath;
        } else if (newPath.startsWith("/")) {
            return HttpUtil.getProtocol(urlFrom).toString() + "://" + HttpUtil.getDomain(urlFrom) + newPath;
        } else if (newPath.startsWith("..")) {
            return urlFrom;
        } else if (newPath.startsWith("./")) {
            urlFrom = urlFrom.substring(0, urlFrom.lastIndexOf("/"));
            return urlFrom + newPath.substring(1);
        } else if (newPath.contains("://")) {
            return newPath;
        } else if (newPath.isEmpty()) {
            return urlFrom;
        } else {
            return HttpUtil.getProtocol(urlFrom).toString() + "://" + HttpUtil.getDomain(urlFrom) + "/" + newPath;
        }
    }

    public static Set<String> getDomainsWithProtocolFromUrls(Collection<String> urls) {
        Set<String> domains = new HashSet<>();
        for (String url : urls) {
            domains.add(HttpUtil.getProtocol(url).toString() + "://" + HttpUtil.getDomain(url));
        }
        return domains;
    }

    public static Set<String> getDomainsFromLinksTo(Collection<Link> links) {
        Set<String> domains = new HashSet<>();
        for (Link link : links) {
            try {
                domains.add(link.to().getProtocol() + "://" + link.to().getHost());
            } catch (Exception ignore) {
            }
        }
        return domains;
    }

    public static String normalizeUrl(String url, String domain, String protocol) {
        if (url.contains("://")) {
            return url;
        }
        if (url.startsWith("//")) {
            url = url.substring(2);
            if (url.contains("://")) {
                return url;
            }
            return "http://".concat(url);
        }
        if (url.startsWith("/")) {
            return domain.concat(url);
        }
        return domain.concat("/").concat(url);
    }

    public static void main(String[] args) {
        System.out.println(HttpUtil.getDomain("www.google.com"));
    }
}
