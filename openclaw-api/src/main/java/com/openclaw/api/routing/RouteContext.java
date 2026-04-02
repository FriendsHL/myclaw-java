package com.openclaw.api.routing;

import com.openclaw.api.message.ChatType;

/**
 * Input context for route resolution.
 * Built from an inbound message's metadata.
 */
public class RouteContext {

    private final String channelId;
    private final String accountId;
    private final String peerId;
    private final ChatType chatType;
    private final String threadId;

    public RouteContext(String channelId, String accountId, String peerId, ChatType chatType, String threadId) {
        this.channelId = channelId;
        this.accountId = accountId;
        this.peerId = peerId;
        this.chatType = chatType;
        this.threadId = threadId;
    }

    public String getChannelId() { return channelId; }
    public String getAccountId() { return accountId; }
    public String getPeerId() { return peerId; }
    public ChatType getChatType() { return chatType; }
    public String getThreadId() { return threadId; }
}
