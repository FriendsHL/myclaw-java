package com.openclaw.api.session;

import java.util.Objects;

/**
 * Deterministic session key: "agent:{agentId}:{channel}/{accountId}/{peer}".
 * Uniquely identifies a conversation session.
 */
public class SessionKey {

    private final String agentId;
    private final String channel;
    private final String accountId;
    private final String peerId;

    public SessionKey(String agentId, String channel, String accountId, String peerId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.peerId = Objects.requireNonNull(peerId, "peerId");
    }

    public String getAgentId() { return agentId; }
    public String getChannel() { return channel; }
    public String getAccountId() { return accountId; }
    public String getPeerId() { return peerId; }

    /** Serialize to the canonical key format */
    public String toKeyString() {
        return String.format("agent:%s:%s/%s/%s", agentId, channel, accountId, peerId);
    }

    /** Parse a key string back to SessionKey */
    public static SessionKey parse(String key) {
        // Format: "agent:{agentId}:{channel}/{accountId}/{peer}"
        if (!key.startsWith("agent:")) {
            throw new IllegalArgumentException("Invalid session key format: " + key);
        }
        String rest = key.substring("agent:".length());
        int colonIdx = rest.indexOf(':');
        if (colonIdx < 0) {
            throw new IllegalArgumentException("Invalid session key format: " + key);
        }
        String agentId = rest.substring(0, colonIdx);
        String[] parts = rest.substring(colonIdx + 1).split("/", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid session key format: " + key);
        }
        return new SessionKey(agentId, parts[0], parts[1], parts[2]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionKey that)) return false;
        return agentId.equals(that.agentId) && channel.equals(that.channel)
                && accountId.equals(that.accountId) && peerId.equals(that.peerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, channel, accountId, peerId);
    }

    @Override
    public String toString() {
        return toKeyString();
    }
}
