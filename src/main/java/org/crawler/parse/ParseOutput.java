package org.crawler.parse;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class ParseOutput {

    private final Map<String, Map<String, String>> output;
    private final Collection<URL> links;

    public ParseOutput(Map<String, Map<String, String>> output, Collection<URL> links) {
        this.output = output;
        this.links = links;
    }

    public Map<String, Map<String, String>> getOutput() {
        return output;
    }

    public Collection<URL> getLinks() {
        return links;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.output);
        hash = 47 * hash + Objects.hashCode(this.links);
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
        final ParseOutput other = (ParseOutput) obj;
        if (!Objects.equals(this.output, other.output)) {
            return false;
        }
        if (!Objects.equals(this.links, other.links)) {
            return false;
        }
        return true;
    }

}
