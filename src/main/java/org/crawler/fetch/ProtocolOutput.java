package org.crawler.fetch;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 *
 */
public final class ProtocolOutput {

    private final ByteBuffer content;
    private final String encoding;
    private final ContentType type;
    private final String url;

    public ProtocolOutput(ByteBuffer content, String encoding, ContentType type, String url) {
        this.content = content;
        this.encoding = encoding;
        this.type = type;
        this.url = url;
    }

    public ByteBuffer getContent() {
        return content;
    }

    public String getEncoding() {
        return encoding;
    }

    public ContentType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.encoding);
        hash = 41 * hash + Objects.hashCode(this.type);
        hash = 41 * hash + Objects.hashCode(this.url);
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
        final ProtocolOutput other = (ProtocolOutput) obj;
        if (!Objects.equals(this.encoding, other.encoding)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

}
