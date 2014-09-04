package org.crawler.fetch;

public enum ProtocolType {

    Http("http", 200),
    Https("https", 200),
    Ftp("ftp", 100),
    Unknown("unknown", 0);
    private final String type;
    private final int validCode;

    private ProtocolType(String type, int validCode) {
        this.type = type;
        this.validCode = validCode;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public int getValidCode() {
        return validCode;
    }

    public static ProtocolType getType(String protocol) {
        for (ProtocolType type : ProtocolType.values()) {
            if (type.toString().equals(protocol.toLowerCase().toString())) {
                return type;
            }
        }
        return null;
    }
}
