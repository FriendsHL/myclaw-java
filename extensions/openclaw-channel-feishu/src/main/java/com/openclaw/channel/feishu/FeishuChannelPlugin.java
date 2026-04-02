package com.openclaw.channel.feishu;

import com.openclaw.api.channel.ChannelCapabilities;
import com.openclaw.api.channel.ChannelConfigAdapter;
import com.openclaw.api.channel.ChannelInbound;
import com.openclaw.api.channel.ChannelLifecycle;
import com.openclaw.api.channel.ChannelMeta;
import com.openclaw.api.channel.ChannelOutbound;
import com.openclaw.api.channel.ChannelPlugin;

/**
 * Feishu (Lark) channel plugin for OpenClaw.
 * Provides webhook-based inbound message reception and Feishu API outbound delivery.
 */
public class FeishuChannelPlugin implements ChannelPlugin {

    private final FeishuInbound inbound;
    private final FeishuOutbound outbound;
    private final FeishuLifecycle lifecycle;
    private final FeishuConfigAdapter configAdapter;

    public FeishuChannelPlugin() {
        this.inbound = new FeishuInbound();
        this.outbound = new FeishuOutbound();
        this.lifecycle = new FeishuLifecycle(outbound);
        this.configAdapter = new FeishuConfigAdapter();
    }

    @Override
    public String id() {
        return "feishu";
    }

    @Override
    public ChannelMeta meta() {
        return new ChannelMeta("Feishu", "Feishu (Lark) messaging channel via event subscription");
    }

    @Override
    public ChannelCapabilities capabilities() {
        return ChannelCapabilities.builder()
                .supportsGroups(true)
                .supportsThreads(true)
                .supportsReactions(true)
                .supportsMedia(true)
                .supportsVoice(false)
                .supportsEditing(false)
                .build();
    }

    @Override
    public ChannelConfigAdapter configAdapter() {
        return configAdapter;
    }

    @Override
    public ChannelLifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public ChannelInbound inbound() {
        return inbound;
    }

    @Override
    public ChannelOutbound outbound() {
        return outbound;
    }
}
