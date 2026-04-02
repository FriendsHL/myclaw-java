package com.openclaw.app;

import com.openclaw.api.channel.InboundMessageListener;
import com.openclaw.api.channel.PluginContext;
import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.core.gateway.GatewayServer;
import com.openclaw.core.plugin.PluginLifecycleManager;
import com.openclaw.core.plugin.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OpenClawApplication {

    private static final Logger log = LoggerFactory.getLogger(OpenClawApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OpenClawApplication.class, args);
    }

    @Bean
    public CommandLineRunner startGateway(OpenClawConfig config,
                                           PluginRegistry pluginRegistry,
                                           PluginLifecycleManager lifecycleManager,
                                           GatewayServer gatewayServer) {
        return args -> {
            log.info("Starting OpenClaw Gateway...");
            log.info("Registered channels: {}", pluginRegistry.getChannelIds());
            log.info("Configured agents: {}", config.getAgents().keySet());

            // Initialize plugins
            String dataDir = System.getProperty("user.home") + "/.openclaw/plugins";
            PluginContext pluginContext = new PluginContext(config, dataDir);
            lifecycleManager.initAll(pluginContext);

            // Connect all configured channel accounts
            InboundMessageListener messageListener = gatewayServer::handleInboundMessage;
            lifecycleManager.connectAll(config, messageListener);

            log.info("OpenClaw Gateway started on port {}",
                    config.getGateway().getPort());
        };
    }
}
