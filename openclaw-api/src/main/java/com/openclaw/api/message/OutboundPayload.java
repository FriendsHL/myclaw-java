package com.openclaw.api.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Normalized outbound message to be sent to a channel.
 */
public class OutboundPayload {

    private final String text;
    private final List<Attachment> attachments;
    private final Map<String, Object> extra;

    public OutboundPayload(String text) {
        this(text, Collections.emptyList(), Collections.emptyMap());
    }

    public OutboundPayload(String text, List<Attachment> attachments, Map<String, Object> extra) {
        this.text = text;
        this.attachments = attachments != null ? List.copyOf(attachments) : Collections.emptyList();
        this.extra = extra != null ? Map.copyOf(extra) : Collections.emptyMap();
    }

    public String getText() { return text; }
    public List<Attachment> getAttachments() { return attachments; }
    public Map<String, Object> getExtra() { return extra; }
}
