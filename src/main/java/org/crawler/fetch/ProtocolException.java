package org.crawler.fetch;

/**
 *
 */
public class ProtocolException extends Exception {

    private String message;

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public ProtocolException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return message;
    }

}
