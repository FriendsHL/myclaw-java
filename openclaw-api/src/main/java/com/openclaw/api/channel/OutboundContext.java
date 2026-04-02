package com.openclaw.api.channel;

/**
 * Target context for sending an outbound message.
 */
public class OutboundContext {

    private final String channelId;
    private final String accountId;
    private final String peerId;
    private final String threadId;

    public OutboundContext(String channelId, String accountId, String peerId, String threadId) {
        this.channelId = channelId;
        this.accountId = accountId;
        this.peerId = peerId;
        this.threadId = threadId;
    }

    public String getChannelId() { return channelId; }
    public String getAccountId() { return accountId; }
    public String getPeerId() { return peerId; }
    public String getThreadId() { return threadId; }
}
