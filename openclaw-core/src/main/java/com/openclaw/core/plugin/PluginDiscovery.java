package com.openclaw.core.plugin;

import com.openclaw.api.channel.ChannelPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Discovers channel plugins via Java SPI (ServiceLoader).
 * Spring-managed plugins are collected separately via auto-configuration.
 */
public class PluginDiscovery {

    private static final Logger log = LoggerFactory.getLogger(PluginDiscovery.class);

    /**
     * Discover all channel plugins available on the classpath via SPI.
     */
    public List<ChannelPlugin> discoverChannelPlugins() {
        ServiceLoader<ChannelPlugin> loader = ServiceLoader.load(ChannelPlugin.class);
        List<ChannelPlugin> plugins = new ArrayList<>();
        for (ChannelPlugin plugin : loader) {
            log.info("Discovered channel plugin via SPI: {} ({})",
                    plugin.id(), plugin.meta().getDisplayName());
            plugins.add(plugin);
        }
        log.info("SPI discovery found {} channel plugin(s)", plugins.size());
        return plugins;
    }
}
