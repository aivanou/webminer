package org.crawler;

/**
 *
 */
public class PageMetadata {

    private final int depth;

    public PageMetadata(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.depth;
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
        final PageMetadata other = (PageMetadata) obj;
        if (this.depth != other.depth) {
            return false;
        }
        return true;
    }

}
