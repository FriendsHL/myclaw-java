package com.openclaw.api.channel;

import com.openclaw.api.config.OpenClawConfig;

/**
 * Lifecycle hooks for a channel plugin.
 * Called by the gateway during plugin initialization and account management.
 */
public interface ChannelLifecycle {

    /** No-op implementation for channels that don't need lifecycle hooks */
    ChannelLifecycle NOOP = new ChannelLifecycle() {};

    /**
     * Called once when the plugin is loaded.
     * Use this to initialize shared resources (HTTP clients, thread pools, etc.).
     */
    default void init(PluginContext context) {}

    /**
     * Called per-account when a channel account should start.
     * For example, start Telegram long polling for a specific bot token.
     */
    default void connect(AccountContext ctx) throws Exception {}

    /**
     * Called per-account when a channel account should stop.
     */
    default void disconnect(AccountContext ctx) throws Exception {}

    /**
     * Called when the config file is reloaded.
     * Plugins can react to config changes (e.g. reconnect with new token).
     */
    default void onConfigReload(OpenClawConfig newConfig) {}
}
