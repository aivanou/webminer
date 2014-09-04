package org.crawler;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class Link implements Serializable {

    private final URL from, to;
    private final String text, type;

    public Link(URL from, URL to, String text, String type) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.type = type;
    }

    public Link(String from, String to) throws MalformedURLException {
        this.from = new URL(from);
        this.to = new URL(to);
        this.text = "default";
        this.type = "default";
    }

    public Link(String from, String to, String text, String type) throws MalformedURLException {
        this.from = new URL(from);
        this.to = new URL(to);
        this.text = text;
        this.type = type;
    }

    public URL from() {
        return from;
    }

    public URL to() {
        return to;
    }

    public String text() {
        return text;
    }

    public String type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Link other = (Link) obj;
        if (this.from != other.from && (this.from == null || !this.from.equals(other.from))) {
            return false;
        }
        if (this.to != other.to && (this.to == null || !this.to.equals(other.to))) {
            return false;
        }
        return !((this.text == null) ? (other.text != null) : !this.text.equals(other.text));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.from != null ? this.from.toString().hashCode() : 0);
        hash = 59 * hash + (this.to != null ? this.to.toString().hashCode() : 0);
        return hash;
    }
}
