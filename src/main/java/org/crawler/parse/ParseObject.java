package org.crawler.parse;

/**
 *
 */
public class ParseObject {

    private final String id;
    private final String regexp;

    public ParseObject(String id, String regexp) {
        this.id = id;
        this.regexp = regexp;
    }

    public String getId() {
        return id;
    }

    public String getRegexp() {
        return regexp;
    }

}
