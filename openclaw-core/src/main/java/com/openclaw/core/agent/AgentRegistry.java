package com.openclaw.core.agent;

import com.openclaw.api.config.AgentConfig;
import com.openclaw.api.config.OpenClawConfig;

import java.util.Map;
import java.util.Optional;

/**
 * Registry for agent configurations.
 * Agents are defined in config, not discovered via SPI.
 */
public class AgentRegistry {

    private volatile OpenClawConfig config;

    public AgentRegistry(OpenClawConfig config) {
        this.config = config;
    }

    public void updateConfig(OpenClawConfig config) {
        this.config = config;
    }

    /** Get agent config by ID */
    public Optional<AgentConfig> get(String agentId) {
        return Optional.ofNullable(config.getAgents().get(agentId));
    }

    /** Get all configured agents */
    public Map<String, AgentConfig> getAll() {
        return config.getAgents();
    }
}
