package com.openclaw.api.event;

import com.openclaw.api.message.InboundMessage;

/**
 * Published when a message is received from any channel.
 */
public class MessageReceivedEvent extends GatewayEvent {

    private final InboundMessage message;

    public MessageReceivedEvent(InboundMessage message) {
        super();
        this.message = message;
    }

    public InboundMessage getMessage() { return message; }
}
