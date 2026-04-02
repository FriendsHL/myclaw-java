package com.openclaw.core.routing;

import com.openclaw.api.message.InboundMessage;
import com.openclaw.api.routing.ResolvedRoute;
import com.openclaw.api.routing.RouteContext;

/**
 * Resolves the target agent and session for an inbound message.
 */
public interface MessageRouter {

    /**
     * Resolve routing for an inbound message.
     * Determines which agent should handle the message and the session key.
     */
    ResolvedRoute resolve(RouteContext context);

    /**
     * Convenience method: build RouteContext from InboundMessage and resolve.
     */
    default ResolvedRoute resolve(InboundMessage message) {
        RouteContext ctx = new RouteContext(
                message.getChannelId(),
                message.getAccountId(),
                message.getPeerId(),
                message.getChatType(),
                message.getThreadId()
        );
        return resolve(ctx);
    }
}
