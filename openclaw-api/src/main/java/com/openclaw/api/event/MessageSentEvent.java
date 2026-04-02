package com.openclaw.api.event;

import com.openclaw.api.message.DeliveryResult;
import com.openclaw.api.message.OutboundPayload;

/**
 * Published when a message is sent to a channel.
 */
public class MessageSentEvent extends GatewayEvent {

    private final String channelId;
    private final String accountId;
    private final String peerId;
    private final OutboundPayload payload;
    private final DeliveryResult result;

    public MessageSentEvent(String channelId, String accountId, String peerId,
                             OutboundPayload payload, DeliveryResult result) {
        super();
        this.channelId = channelId;
        this.accountId = accountId;
        this.peerId = peerId;
        this.payload = payload;
        this.result = result;
    }

    public String getChannelId() { return channelId; }
    public String getAccountId() { return accountId; }
    public String getPeerId() { return peerId; }
    public OutboundPayload getPayload() { return payload; }
    public DeliveryResult getResult() { return result; }
}
