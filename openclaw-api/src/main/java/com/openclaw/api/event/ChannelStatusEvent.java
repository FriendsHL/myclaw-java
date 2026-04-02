package com.openclaw.api.event;

/**
 * Published when a channel's connection status changes.
 */
public class ChannelStatusEvent extends GatewayEvent {

    public enum Status { CONNECTED, DISCONNECTED, ERROR }

    private final String channelId;
    private final String accountId;
    private final Status status;
    private final String message;

    public ChannelStatusEvent(String channelId, String accountId, Status status, String message) {
        super();
        this.channelId = channelId;
        this.accountId = accountId;
        this.status = status;
        this.message = message;
    }

    public String getChannelId() { return channelId; }
    public String getAccountId() { return accountId; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
}
