package com.openclaw.api.channel;

/**
 * Core contract for all messaging channel plugins.
 * <p>
 * Implementations register via Java SPI ({@code META-INF/services/com.openclaw.api.channel.ChannelPlugin})
 * or Spring Boot auto-configuration.
 * <p>
 * Each channel plugin represents a messaging platform (Telegram, Discord, Slack, etc.)
 * and handles inbound message reception and outbound message delivery.
 */
public interface ChannelPlugin {

    /**
     * Unique channel identifier, e.g. "telegram", "discord", "slack".
     * Must be stable across restarts and match config keys.
     */
    String id();

    /** Human-readable metadata (display name, description, icon) */
    ChannelMeta meta();

    /** Feature flags (supports groups, threads, reactions, media, etc.) */
    ChannelCapabilities capabilities();

    /** Account config resolution - list and resolve per-account configs */
    ChannelConfigAdapter configAdapter();

    /** Lifecycle management (init / connect / disconnect / config reload) */
    default ChannelLifecycle lifecycle() {
        return ChannelLifecycle.NOOP;
    }

    /** Inbound message listener registration */
    ChannelInbound inbound();

    /** Outbound message sending */
    ChannelOutbound outbound();

    /** Interactive setup wizard (optional) */
    default ChannelSetup setup() {
        return null;
    }
}
