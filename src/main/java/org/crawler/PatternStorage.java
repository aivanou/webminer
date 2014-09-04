package org.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * the shell class for pattern collection patterns have structure: patternName
 * -- collection of patterns example: price [ div[id=price ], div[class=field] ]
 */
public class PatternStorage {

    private final Map<String, Collection<String>> patterns;

    public PatternStorage() {
        this.patterns = new HashMap<>();
    }

    public void putPatterns(String name, Collection<String> pts) {
        patterns.put(name, pts);
    }

    public void putPattern(String name, String pattern) {
        if (!patterns.containsKey(name)) {
            patterns.put(name, new ArrayList<String>());
        }
        patterns.get(name).add(pattern);
    }

    public Collection<String> getPatterns(String name) {
        return patterns.get(name);
    }

    public Collection<String> getKeySet() {
        return patterns.keySet();
    }

    public boolean isEmpty() {
        return patterns.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.patterns);
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
        final PatternStorage other = (PatternStorage) obj;
        if (!Objects.equals(this.patterns, other.patterns)) {
            return false;
        }
        return true;
    }

}
