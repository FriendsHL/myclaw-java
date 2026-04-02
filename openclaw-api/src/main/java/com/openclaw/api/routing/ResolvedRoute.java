package com.openclaw.api.routing;

import com.openclaw.api.session.SessionKey;

/**
 * Result of route resolution: which agent handles this message and the session key.
 */
public class ResolvedRoute {

    public enum MatchedBy {
        BINDING_PEER,
        BINDING_ACCOUNT,
        BINDING_CHANNEL,
        DEFAULT
    }

    private final String agentId;
    private final SessionKey sessionKey;
    private final MatchedBy matchedBy;

    public ResolvedRoute(String agentId, SessionKey sessionKey, MatchedBy matchedBy) {
        this.agentId = agentId;
        this.sessionKey = sessionKey;
        this.matchedBy = matchedBy;
    }

    public String getAgentId() { return agentId; }
    public SessionKey getSessionKey() { return sessionKey; }
    public MatchedBy getMatchedBy() { return matchedBy; }
}
