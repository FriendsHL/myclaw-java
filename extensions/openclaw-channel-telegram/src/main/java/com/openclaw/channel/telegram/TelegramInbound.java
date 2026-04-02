package com.openclaw.channel.telegram;

import com.openclaw.api.channel.ChannelInbound;
import com.openclaw.api.channel.InboundMessageListener;

/**
 * Inbound message listener holder for the Telegram channel.
 * Stores the listener reference so that the lifecycle (polling consumer)
 * can forward received messages to the gateway.
 */
public class TelegramInbound implements ChannelInbound {

    private volatile InboundMessageListener listener;

    @Override
    public void register(InboundMessageListener listener) {
        this.listener = listener;
    }

    /** Access the registered listener from other components (e.g. lifecycle). */
    public InboundMessageListener getListener() {
        return listener;
    }
}
