package com.openclaw.api.message;

/**
 * A media attachment (image, file, audio, video) on a message.
 */
public class Attachment {

    public enum Type { IMAGE, FILE, AUDIO, VIDEO }

    private final Type type;
    private final String url;
    private final String fileName;
    private final String mimeType;
    private final Long sizeBytes;

    public Attachment(Type type, String url, String fileName, String mimeType, Long sizeBytes) {
        this.type = type;
        this.url = url;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }

    public Type getType() { return type; }
    public String getUrl() { return url; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
}
