package com.openclaw.core.gateway;

import com.openclaw.api.channel.ChannelPlugin;
import com.openclaw.core.plugin.PluginRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for gateway status and channel management.
 */
@RestController
@RequestMapping("/api")
public class GatewayRestController {

    private final PluginRegistry pluginRegistry;

    public GatewayRestController(PluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "running");
        result.put("version", "0.1.0-SNAPSHOT");
        result.put("channels", pluginRegistry.size());
        return result;
    }

    @GetMapping("/channels")
    public List<Map<String, Object>> channels() {
        return pluginRegistry.getAll().stream()
                .map(this::channelInfo)
                .collect(Collectors.toList());
    }

    private Map<String, Object> channelInfo(ChannelPlugin plugin) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", plugin.id());
        info.put("name", plugin.meta().getDisplayName());
        info.put("description", plugin.meta().getDescription());
        info.put("capabilities", Map.of(
                "groups", plugin.capabilities().supportsGroups(),
                "threads", plugin.capabilities().supportsThreads(),
                "media", plugin.capabilities().supportsMedia(),
                "reactions", plugin.capabilities().supportsReactions()
        ));
        return info;
    }
}
