package com.openclaw.api.channel;

import com.openclaw.api.message.DeliveryResult;
import com.openclaw.api.message.OutboundPayload;

/**
 * Sends messages to a channel's platform API.
 */
public interface ChannelOutbound {

    /**
     * Send a message to the channel.
     *
     * @param context target account + peer + thread info
     * @param payload text, media, or structured message content
     * @return delivery result with platform message ID
     */
    DeliveryResult send(OutboundContext context, OutboundPayload payload);
}
