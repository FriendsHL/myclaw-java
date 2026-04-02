package com.openclaw.core.plugin;

import com.openclaw.api.channel.*;
import com.openclaw.api.config.ChannelConfig;
import com.openclaw.api.config.OpenClawConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Manages plugin lifecycle: init, connect accounts, disconnect accounts.
 */
public class PluginLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(PluginLifecycleManager.class);

    private final PluginRegistry registry;

    public PluginLifecycleManager(PluginRegistry registry) {
        this.registry = registry;
    }

    /**
     * Initialize all plugins with the runtime context.
     */
    public void initAll(PluginContext context) {
        for (ChannelPlugin plugin : registry.getAll()) {
            try {
                plugin.lifecycle().init(context);
                log.info("Initialized plugin: {}", plugin.id());
            } catch (Exception e) {
                log.error("Failed to initialize plugin: {}", plugin.id(), e);
            }
        }
    }

    /**
     * Connect all configured accounts across all channels.
     */
    public void connectAll(OpenClawConfig config, InboundMessageListener messageListener) {
        for (ChannelPlugin plugin : registry.getAll()) {
            String channelId = plugin.id();
            ChannelConfig channelConfig = config.getChannels().get(channelId);
            if (channelConfig == null) {
                log.debug("No config for channel: {}, skipping", channelId);
                continue;
            }

            for (String accountId : plugin.configAdapter().listAccountIds(config)) {
                Map<String, Object> accountConfig = plugin.configAdapter()
                        .resolveAccountConfig(config, accountId);
                if (!plugin.configAdapter().isEnabled(accountConfig)) {
                    log.info("Account disabled: {}/{}", channelId, accountId);
                    continue;
                }

                try {
                    AccountContext ctx = new AccountContext(
                            channelId, accountId, config, accountConfig, messageListener);
                    plugin.lifecycle().connect(ctx);
                    log.info("Connected: {}/{}", channelId, accountId);
                } catch (Exception e) {
                    log.error("Failed to connect: {}/{}", channelId, accountId, e);
                }
            }
        }
    }

    /**
     * Disconnect all accounts across all channels.
     */
    public void disconnectAll(OpenClawConfig config) {
        for (ChannelPlugin plugin : registry.getAll()) {
            for (String accountId : plugin.configAdapter().listAccountIds(config)) {
                try {
                    Map<String, Object> accountConfig = plugin.configAdapter()
                            .resolveAccountConfig(config, accountId);
                    AccountContext ctx = new AccountContext(
                            plugin.id(), accountId, config, accountConfig, msg -> {});
                    plugin.lifecycle().disconnect(ctx);
                    log.info("Disconnected: {}/{}", plugin.id(), accountId);
                } catch (Exception e) {
                    log.error("Failed to disconnect: {}/{}", plugin.id(), accountId, e);
                }
            }
        }
    }
}
