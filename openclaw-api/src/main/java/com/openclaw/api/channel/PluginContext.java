package com.openclaw.api.channel;

import com.openclaw.api.config.OpenClawConfig;

/**
 * Runtime context provided to channel plugins during initialization.
 * Gives plugins access to shared gateway resources.
 */
public class PluginContext {

    private final OpenClawConfig config;
    private final String dataDir;

    public PluginContext(OpenClawConfig config, String dataDir) {
        this.config = config;
        this.dataDir = dataDir;
    }

    /** Current gateway configuration */
    public OpenClawConfig getConfig() { return config; }

    /** Base directory for plugin data storage (e.g. ~/.openclaw/plugins/{id}/) */
    public String getDataDir() { return dataDir; }
}
