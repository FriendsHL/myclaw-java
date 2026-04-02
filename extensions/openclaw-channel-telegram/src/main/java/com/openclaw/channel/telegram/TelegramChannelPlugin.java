package com.openclaw.channel.telegram;

import com.openclaw.api.channel.ChannelCapabilities;
import com.openclaw.api.channel.ChannelConfigAdapter;
import com.openclaw.api.channel.ChannelInbound;
import com.openclaw.api.channel.ChannelLifecycle;
import com.openclaw.api.channel.ChannelMeta;
import com.openclaw.api.channel.ChannelOutbound;
import com.openclaw.api.channel.ChannelPlugin;

/**
 * Telegram channel plugin for OpenClaw.
 * Provides long-polling-based inbound message reception and Bot API outbound delivery.
 */
public class TelegramChannelPlugin implements ChannelPlugin {

    private final TelegramInbound inbound;
    private final TelegramOutbound outbound;
    private final TelegramLifecycle lifecycle;
    private final TelegramConfigAdapter configAdapter;

    public TelegramChannelPlugin() {
        this.inbound = new TelegramInbound();
        this.outbound = new TelegramOutbound();
        this.lifecycle = new TelegramLifecycle(outbound);
        this.configAdapter = new TelegramConfigAdapter();
    }

    @Override
    public String id() {
        return "telegram";
    }

    @Override
    public ChannelMeta meta() {
        return new ChannelMeta("Telegram", "Telegram Bot API messaging channel");
    }

    @Override
    public ChannelCapabilities capabilities() {
        return ChannelCapabilities.builder()
                .supportsGroups(true)
                .supportsThreads(false)
                .supportsReactions(true)
                .supportsMedia(true)
                .supportsVoice(false)
                .supportsEditing(true)
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
