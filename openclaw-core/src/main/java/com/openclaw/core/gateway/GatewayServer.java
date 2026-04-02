package com.openclaw.core.gateway;

import com.openclaw.api.agent.AgentContext;
import com.openclaw.api.agent.AgentResult;
import com.openclaw.api.channel.ChannelPlugin;
import com.openclaw.api.channel.OutboundContext;
import com.openclaw.api.config.AgentConfig;
import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.api.event.MessageReceivedEvent;
import com.openclaw.api.event.MessageSentEvent;
import com.openclaw.api.message.DeliveryResult;
import com.openclaw.api.message.InboundMessage;
import com.openclaw.api.message.OutboundPayload;
import com.openclaw.api.routing.ResolvedRoute;
import com.openclaw.api.session.SessionEntry;
import com.openclaw.core.agent.AgentRegistry;
import com.openclaw.core.agent.AgentRunner;
import com.openclaw.core.plugin.PluginRegistry;
import com.openclaw.core.routing.AllowlistChecker;
import com.openclaw.core.routing.MessageRouter;
import com.openclaw.core.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * Core gateway server: orchestrates the full message flow.
 * <p>
 * Inbound message -> allowlist check -> routing -> session -> agent -> outbound
 */
public class GatewayServer {

    private static final Logger log = LoggerFactory.getLogger(GatewayServer.class);

    private final PluginRegistry pluginRegistry;
    private final MessageRouter messageRouter;
    private final SessionManager sessionManager;
    private final AgentRunner agentRunner;
    private final AgentRegistry agentRegistry;
    private final AllowlistChecker allowlistChecker;
    private final ApplicationEventPublisher eventPublisher;

    public GatewayServer(PluginRegistry pluginRegistry,
                          MessageRouter messageRouter,
                          SessionManager sessionManager,
                          AgentRunner agentRunner,
                          AgentRegistry agentRegistry,
                          AllowlistChecker allowlistChecker,
                          ApplicationEventPublisher eventPublisher) {
        this.pluginRegistry = pluginRegistry;
        this.messageRouter = messageRouter;
        this.sessionManager = sessionManager;
        this.agentRunner = agentRunner;
        this.agentRegistry = agentRegistry;
        this.allowlistChecker = allowlistChecker;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handle an inbound message from any channel.
     * This is called by channel plugins when they receive a message.
     */
    public void handleInboundMessage(InboundMessage message) {
        log.info("Inbound message from {}/{}: {}",
                message.getChannelId(), message.getSenderId(),
                truncate(message.getText(), 100));

        // Publish event
        eventPublisher.publishEvent(new MessageReceivedEvent(message));

        // Allowlist check
        if (!allowlistChecker.isAllowed(message)) {
            log.info("Message rejected by allowlist: {}/{}", message.getChannelId(), message.getSenderId());
            return;
        }

        // Resolve route
        ResolvedRoute route = messageRouter.resolve(message);
        log.debug("Routed to agent '{}' via {}", route.getAgentId(), route.getMatchedBy());

        // Load agent config
        AgentConfig agentConfig = agentRegistry.get(route.getAgentId()).orElse(null);
        if (agentConfig == null) {
            log.error("Agent not found: {}", route.getAgentId());
            return;
        }

        // Load session history and append user message
        List<SessionEntry> history = sessionManager.getHistory(route.getSessionKey());
        sessionManager.appendUserMessage(route.getSessionKey(), message.getText());

        // Run agent
        AgentContext agentContext = new AgentContext(route, message, agentConfig, history);
        AgentResult result = agentRunner.process(agentContext);

        if (!result.isSuccess()) {
            log.error("Agent failed: {}", result.getErrorMessage());
            return;
        }

        // Append assistant message to session
        sessionManager.appendAssistantMessage(route.getSessionKey(), result.getResponseText());

        // Send outbound message
        sendOutbound(message, result.getOutboundPayload());
    }

    private void sendOutbound(InboundMessage inbound, OutboundPayload payload) {
        ChannelPlugin plugin = pluginRegistry.get(inbound.getChannelId()).orElse(null);
        if (plugin == null) {
            log.error("Channel plugin not found: {}", inbound.getChannelId());
            return;
        }

        OutboundContext outCtx = new OutboundContext(
                inbound.getChannelId(),
                inbound.getAccountId(),
                inbound.getPeerId(),
                inbound.getThreadId()
        );

        DeliveryResult deliveryResult = plugin.outbound().send(outCtx, payload);

        // Publish sent event
        eventPublisher.publishEvent(new MessageSentEvent(
                inbound.getChannelId(), inbound.getAccountId(), inbound.getPeerId(),
                payload, deliveryResult));

        if (deliveryResult.isSuccess()) {
            log.info("Reply sent to {}/{}", inbound.getChannelId(), inbound.getPeerId());
        } else {
            log.error("Failed to send reply: {}", deliveryResult.getErrorMessage());
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "<null>";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
