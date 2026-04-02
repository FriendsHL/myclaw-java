package com.openclaw.api.channel;

/**
 * Handles inbound message listener registration.
 * The channel plugin calls the registered listener when a message arrives.
 */
@FunctionalInterface
public interface ChannelInbound {

    /**
     * Register the listener that the gateway provides.
     * When the channel receives a message, normalize it to InboundMessage
     * and call {@code listener.onMessage(message)}.
     */
    void register(InboundMessageListener listener);
}
