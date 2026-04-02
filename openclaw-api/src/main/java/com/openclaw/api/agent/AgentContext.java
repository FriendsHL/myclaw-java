package com.openclaw.api.agent;

import com.openclaw.api.config.AgentConfig;
import com.openclaw.api.message.InboundMessage;
import com.openclaw.api.routing.ResolvedRoute;
import com.openclaw.api.session.SessionEntry;

import java.util.List;

/**
 * All context needed for an agent to process a message.
 */
public class AgentContext {

    private final ResolvedRoute route;
    private final InboundMessage message;
    private final AgentConfig agentConfig;
    private final List<SessionEntry> history;

    public AgentContext(ResolvedRoute route, InboundMessage message,
                        AgentConfig agentConfig, List<SessionEntry> history) {
        this.route = route;
        this.message = message;
        this.agentConfig = agentConfig;
        this.history = history;
    }

    public ResolvedRoute getRoute() { return route; }
    public InboundMessage getMessage() { return message; }
    public AgentConfig getAgentConfig() { return agentConfig; }
    public List<SessionEntry> getHistory() { return history; }
}
