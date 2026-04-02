package com.openclaw.api.channel;

import com.openclaw.api.message.InboundMessage;

/**
 * Callback interface for receiving normalized inbound messages from channels.
 */
@FunctionalInterface
public interface InboundMessageListener {

    /**
     * Called when a channel receives a message from a user.
     * The message has already been normalized to the OpenClaw format.
     */
    void onMessage(InboundMessage message);
}
