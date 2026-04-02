package com.openclaw.core.routing;

import com.openclaw.api.config.BindingConfig;
import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.api.routing.ResolvedRoute;
import com.openclaw.api.routing.RouteContext;
import com.openclaw.api.session.SessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default routing implementation.
 * <p>
 * Resolution order (most specific first):
 * 1. Binding with matching channel + account + peer
 * 2. Binding with matching channel + account (no peer)
 * 3. Binding with matching channel only
 * 4. Default agent ("main")
 */
public class DefaultMessageRouter implements MessageRouter {

    private static final Logger log = LoggerFactory.getLogger(DefaultMessageRouter.class);
    private static final String DEFAULT_AGENT_ID = "main";

    private volatile OpenClawConfig config;

    public DefaultMessageRouter(OpenClawConfig config) {
        this.config = config;
    }

    public void updateConfig(OpenClawConfig config) {
        this.config = config;
    }

    @Override
    public ResolvedRoute resolve(RouteContext ctx) {
        List<BindingConfig> bindings = config.getBindings();

        // 1. Peer-level match (most specific)
        for (BindingConfig binding : bindings) {
            if (matches(binding.getChannel(), ctx.getChannelId())
                    && matches(binding.getAccountId(), ctx.getAccountId())
                    && matches(binding.getPeer(), ctx.getPeerId())) {
                return buildRoute(binding.getAgentId(), ctx, ResolvedRoute.MatchedBy.BINDING_PEER);
            }
        }

        // 2. Account-level match
        for (BindingConfig binding : bindings) {
            if (matches(binding.getChannel(), ctx.getChannelId())
                    && matches(binding.getAccountId(), ctx.getAccountId())
                    && binding.getPeer() == null) {
                return buildRoute(binding.getAgentId(), ctx, ResolvedRoute.MatchedBy.BINDING_ACCOUNT);
            }
        }

        // 3. Channel-level match
        for (BindingConfig binding : bindings) {
            if (matches(binding.getChannel(), ctx.getChannelId())
                    && binding.getAccountId() == null
                    && binding.getPeer() == null) {
                return buildRoute(binding.getAgentId(), ctx, ResolvedRoute.MatchedBy.BINDING_CHANNEL);
            }
        }

        // 4. Default
        log.debug("No binding found for {}/{}/{}, using default agent",
                ctx.getChannelId(), ctx.getAccountId(), ctx.getPeerId());
        return buildRoute(DEFAULT_AGENT_ID, ctx, ResolvedRoute.MatchedBy.DEFAULT);
    }

    private boolean matches(String bindingValue, String actual) {
        if (bindingValue == null) return true;
        return bindingValue.equals(actual);
    }

    private ResolvedRoute buildRoute(String agentId, RouteContext ctx, ResolvedRoute.MatchedBy matchedBy) {
        SessionKey sessionKey = new SessionKey(
                agentId,
                ctx.getChannelId(),
                ctx.getAccountId() != null ? ctx.getAccountId() : "default",
                ctx.getPeerId() != null ? ctx.getPeerId() : "default"
        );
        return new ResolvedRoute(agentId, sessionKey, matchedBy);
    }
}
