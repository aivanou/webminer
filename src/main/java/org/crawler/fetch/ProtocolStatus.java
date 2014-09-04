package org.crawler.fetch;

import java.util.Objects;

/**
 *
 */
public class ProtocolStatus {

    public enum Type {

        Valid,
        Moved,
        NotFound,
        UnknownHost,
        UnknownProtocol,
        NotModified,
        ServerError,
        Unauthorized,
        UnknownState,
        InvalidPath,
        TooManyRedirections,
        UnknownError;
    }

    private final Object message;
    private final Type state;

    public ProtocolStatus(Object message, Type state) {
        this.message = message;
        this.state = state;
    }

    public Object getMessage() {
        return message;
    }

    public Type getState() {
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.message);
        hash = 17 * hash + Objects.hashCode(this.state);
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
        final ProtocolStatus other = (ProtocolStatus) obj;
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        return this.state == other.state;
    }

    @Override
    public String toString() {
        return state.toString() + " { " + message.toString() + " }";
    }

}
