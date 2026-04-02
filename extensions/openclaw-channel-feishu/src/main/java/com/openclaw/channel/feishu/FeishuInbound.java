package com.openclaw.channel.feishu;

import com.openclaw.api.channel.ChannelInbound;
import com.openclaw.api.channel.InboundMessageListener;

/**
 * Inbound message listener holder for the Feishu channel.
 * Stores the listener reference so that the event handler (webhook callback)
 * can forward received messages to the gateway.
 */
public class FeishuInbound implements ChannelInbound {

    private volatile InboundMessageListener listener;

    @Override
    public void register(InboundMessageListener listener) {
        this.listener = listener;
    }

    /** Access the registered listener from other components (e.g. event handler). */
    public InboundMessageListener getListener() {
        return listener;
    }
}
