package org.crawler.fetch;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public abstract class ProtocolFetcher {

    public abstract ProtocolResponse fetch(String path) throws ProtocolException;

    public abstract ProtocolResponse fetch(String path, URL porxy) throws ProtocolException;
    private static final Map<ProtocolType, ProtocolFetcher> fetchers = new EnumMap<>(ProtocolType.class);

    public static void build() {
        fetchers.put(ProtocolType.Http, HttpFetcher.init());
        fetchers.put(ProtocolType.Https, HttpFetcher.init());
    }

    public static ProtocolFetcher getFetcher(ProtocolType type) {
        if (fetchers.containsKey(type)) {
            return fetchers.get(type);
        }
        return null;
    }
}
