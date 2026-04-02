package com.openclaw.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Root configuration for OpenClaw.
 * Binds from application.yml under the "openclaw" prefix via Spring Boot
 * {@link ConfigurationProperties}, and also supports Jackson deserialization
 * for backward compatibility.
 */
@ConfigurationProperties(prefix = "openclaw")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenClawConfig {

    @NestedConfigurationProperty
    private GatewayConfig gateway;

    private Map<String, AgentConfig> agents;
    private Map<String, ChannelConfig> channels;
    private Map<String, ModelConfig> models;
    private List<BindingConfig> bindings;

    public GatewayConfig getGateway() {
        return gateway != null ? gateway : new GatewayConfig();
    }
    public void setGateway(GatewayConfig gateway) { this.gateway = gateway; }

    public Map<String, AgentConfig> getAgents() {
        return agents != null ? agents : Collections.emptyMap();
    }
    public void setAgents(Map<String, AgentConfig> agents) { this.agents = agents; }

    public Map<String, ChannelConfig> getChannels() {
        return channels != null ? channels : Collections.emptyMap();
    }
    public void setChannels(Map<String, ChannelConfig> channels) { this.channels = channels; }

    public Map<String, ModelConfig> getModels() {
        return models != null ? models : Collections.emptyMap();
    }
    public void setModels(Map<String, ModelConfig> models) { this.models = models; }

    public List<BindingConfig> getBindings() {
        return bindings != null ? bindings : Collections.emptyList();
    }
    public void setBindings(List<BindingConfig> bindings) { this.bindings = bindings; }
}
