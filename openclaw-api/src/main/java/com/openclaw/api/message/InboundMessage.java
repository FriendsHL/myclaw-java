package com.openclaw.api.message;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Normalized inbound message from any channel.
 * All channel-specific formats are converted to this common representation.
 */
public class InboundMessage {

    private final String channelId;
    private final String accountId;
    private final String peerId;
    private final ChatType chatType;
    private final String threadId;
    private final String senderId;
    private final String senderName;
    private final String text;
    private final List<Attachment> attachments;
    private final Instant timestamp;
    private final Map<String, Object> extra;

    private InboundMessage(Builder builder) {
        this.channelId = builder.channelId;
        this.accountId = builder.accountId;
        this.peerId = builder.peerId;
        this.chatType = builder.chatType;
        this.threadId = builder.threadId;
        this.senderId = builder.senderId;
        this.senderName = builder.senderName;
        this.text = builder.text;
        this.attachments = builder.attachments != null ? List.copyOf(builder.attachments) : Collections.emptyList();
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.extra = builder.extra != null ? Map.copyOf(builder.extra) : Collections.emptyMap();
    }

    public String getChannelId() { return channelId; }
    public String getAccountId() { return accountId; }
    public String getPeerId() { return peerId; }
    public ChatType getChatType() { return chatType; }
    public String getThreadId() { return threadId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public List<Attachment> getAttachments() { return attachments; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getExtra() { return extra; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String channelId;
        private String accountId;
        private String peerId;
        private ChatType chatType = ChatType.DM;
        private String threadId;
        private String senderId;
        private String senderName;
        private String text;
        private List<Attachment> attachments;
        private Instant timestamp;
        private Map<String, Object> extra;

        public Builder channelId(String v) { this.channelId = v; return this; }
        public Builder accountId(String v) { this.accountId = v; return this; }
        public Builder peerId(String v) { this.peerId = v; return this; }
        public Builder chatType(ChatType v) { this.chatType = v; return this; }
        public Builder threadId(String v) { this.threadId = v; return this; }
        public Builder senderId(String v) { this.senderId = v; return this; }
        public Builder senderName(String v) { this.senderName = v; return this; }
        public Builder text(String v) { this.text = v; return this; }
        public Builder attachments(List<Attachment> v) { this.attachments = v; return this; }
        public Builder timestamp(Instant v) { this.timestamp = v; return this; }
        public Builder extra(Map<String, Object> v) { this.extra = v; return this; }

        public InboundMessage build() { return new InboundMessage(this); }
    }
}
