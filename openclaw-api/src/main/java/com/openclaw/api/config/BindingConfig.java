package com.openclaw.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A routing binding: maps a channel/account/peer combination to a specific agent.
 * Spring Boot automatically maps kebab-case properties (e.g. account-id, agent-id)
 * to camelCase fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BindingConfig {

    private String channel;
    private String accountId;
    private String peer;
    private String agentId;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getPeer() { return peer; }
    public void setPeer(String peer) { this.peer = peer; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
}
