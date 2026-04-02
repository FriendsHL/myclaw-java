package com.openclaw.api.channel;

import com.openclaw.api.config.OpenClawConfig;

import java.util.List;
import java.util.Map;

/**
 * Resolves account configurations for a channel from the global config.
 */
public interface ChannelConfigAdapter {

    /** List all configured account IDs for this channel */
    List<String> listAccountIds(OpenClawConfig config);

    /** Resolve the raw account config map for a given account ID */
    Map<String, Object> resolveAccountConfig(OpenClawConfig config, String accountId);

    /** Check if an account is enabled (default: true) */
    default boolean isEnabled(Map<String, Object> accountConfig) {
        return true;
    }

    /** Check if an account has minimum required configuration */
    default boolean isConfigured(Map<String, Object> accountConfig) {
        return true;
    }
}
