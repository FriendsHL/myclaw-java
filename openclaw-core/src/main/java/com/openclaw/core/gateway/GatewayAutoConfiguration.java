package com.openclaw.core.gateway;

import com.openclaw.api.channel.ChannelPlugin;
import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.core.agent.AgentRegistry;
import com.openclaw.core.agent.AgentRunner;
import com.openclaw.core.agent.ChatModelResolver;
import com.openclaw.core.plugin.PluginDiscovery;
import com.openclaw.core.plugin.PluginLifecycleManager;
import com.openclaw.core.plugin.PluginRegistry;
import com.openclaw.core.routing.AllowlistChecker;
import com.openclaw.core.routing.DefaultMessageRouter;
import com.openclaw.core.routing.MessageRouter;
import com.openclaw.core.session.FileSessionStore;
import com.openclaw.core.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Spring Boot auto-configuration for the OpenClaw gateway core.
 * Sets up all core beans: config (via ConfigurationProperties), plugins,
 * routing, sessions, agents.
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenClawConfig.class)
public class GatewayAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GatewayAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public PluginRegistry pluginRegistry(List<ChannelPlugin> springPlugins) {
        PluginRegistry registry = new PluginRegistry();

        // Register Spring-managed plugins
        for (ChannelPlugin plugin : springPlugins) {
            registry.register(plugin);
        }

        // Also discover SPI plugins not already registered
        PluginDiscovery discovery = new PluginDiscovery();
        for (ChannelPlugin plugin : discovery.discoverChannelPlugins()) {
            if (registry.get(plugin.id()).isEmpty()) {
                registry.register(plugin);
            }
        }

        log.info("Total registered channel plugins: {}", registry.size());
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginLifecycleManager pluginLifecycleManager(PluginRegistry registry) {
        return new PluginLifecycleManager(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageRouter messageRouter(OpenClawConfig config) {
        return new DefaultMessageRouter(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public AllowlistChecker allowlistChecker(OpenClawConfig config) {
        return new AllowlistChecker(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager() {
        return new SessionManager(new FileSessionStore());
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentRegistry agentRegistry(OpenClawConfig config) {
        return new AgentRegistry(config);
    }

    @Bean
    @ConditionalOnMissingBean(ChatModelResolver.class)
    public ChatModelResolver chatModelResolver() {
        // Default no-op resolver; provider modules override this
        return (modelId, modelConfig) -> {
            throw new IllegalStateException(
                    "No AI provider configured. Add a provider dependency "
                    + "(e.g. openclaw-provider-openai) to enable AI capabilities.");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentRunner agentRunner(ChatModelResolver chatModelResolver, OpenClawConfig config) {
        return new AgentRunner(chatModelResolver, config);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayServer gatewayServer(PluginRegistry pluginRegistry,
                                        MessageRouter messageRouter,
                                        SessionManager sessionManager,
                                        AgentRunner agentRunner,
                                        AgentRegistry agentRegistry,
                                        AllowlistChecker allowlistChecker,
                                        ApplicationEventPublisher eventPublisher) {
        return new GatewayServer(pluginRegistry, messageRouter, sessionManager,
                agentRunner, agentRegistry, allowlistChecker, eventPublisher);
    }

    @Bean
    public GatewayRestController gatewayRestController(PluginRegistry pluginRegistry) {
        return new GatewayRestController(pluginRegistry);
    }
}
