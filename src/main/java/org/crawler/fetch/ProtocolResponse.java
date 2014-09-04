package org.crawler.fetch;

import java.util.Objects;

/**
 *
 */
public class ProtocolResponse {

    private final ProtocolStatus statusState;
    private final ProtocolOutput content;

    public ProtocolResponse(ProtocolStatus statusState, ProtocolOutput content) {
        this.statusState = statusState;
        this.content = content;
    }

    public ProtocolStatus getStatusState() {
        return statusState;
    }

    public ProtocolOutput getProtocolOutput() {
        return content;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.statusState);
        hash = 17 * hash + Objects.hashCode(this.content);
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
        final ProtocolResponse other = (ProtocolResponse) obj;
        if (!Objects.equals(this.statusState, other.statusState)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        return true;
    }

}
