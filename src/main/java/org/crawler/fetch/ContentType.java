package org.crawler.fetch;

/**
 *
 */
public enum ContentType {

    Video("video", "video types"),
    Image("image", "some types for image"),
    TextHtml("text", "html"),
    TextHtml5("text", "html"),
    PDFText("text", "pdf"),
    PDFApplication("application", "pdf"),
    Unknown("unknown", "unknown");
    private final String type;
    private final String docType;

    private ContentType(String docType, String type) {
        this.type = type;
        this.docType = docType;
    }

    public String getDocType() {
        return docType;
    }

    public String getType() {
        return type;
    }

    public static ContentType getPageType(String type, String docType) {
        type = type.trim().toLowerCase();
        docType = docType.trim().toLowerCase();
        if (type == null || docType == null) {
            return ContentType.Unknown;
        }
        for (ContentType page : ContentType.values()) {
            if (page.getDocType().equals(docType) && page.getType().equals(type)) {
                return page;
            }
        }
        return ContentType.Unknown;
    }

    public static ContentType forName(String line) {
        if (line == null) {
            return ContentType.Unknown;
        }
        line = line.trim();
        for (ContentType ct : ContentType.values()) {
            if ((ct.getDocType() + "/" + ct.getType()).equals(line)) {
                return ct;
            }
        }
        return ContentType.Unknown;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
