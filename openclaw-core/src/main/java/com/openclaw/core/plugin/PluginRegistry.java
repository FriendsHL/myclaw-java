package com.openclaw.core.plugin;

import com.openclaw.api.channel.ChannelPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Registry for discovered channel plugins.
 * Ensures plugin ID uniqueness and provides lookup.
 */
public class PluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);
    private final Map<String, ChannelPlugin> plugins = new LinkedHashMap<>();

    public void register(ChannelPlugin plugin) {
        String id = plugin.id();
        if (plugins.containsKey(id)) {
            throw new IllegalStateException("Duplicate channel plugin ID: " + id);
        }
        plugins.put(id, plugin);
        log.info("Registered channel plugin: {} ({})", id, plugin.meta().getDisplayName());
    }

    public Optional<ChannelPlugin> get(String channelId) {
        return Optional.ofNullable(plugins.get(channelId));
    }

    public Collection<ChannelPlugin> getAll() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    public Set<String> getChannelIds() {
        return Collections.unmodifiableSet(plugins.keySet());
    }

    public int size() {
        return plugins.size();
    }
}
