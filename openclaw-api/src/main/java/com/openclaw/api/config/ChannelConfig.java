package com.openclaw.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.Map;

/**
 * Per-channel config containing account configurations.
 * The accounts map values are generic maps because each channel has its own schema.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelConfig {

    private Map<String, Map<String, Object>> accounts;

    public Map<String, Map<String, Object>> getAccounts() {
        return accounts != null ? accounts : Collections.emptyMap();
    }

    public void setAccounts(Map<String, Map<String, Object>> accounts) {
        this.accounts = accounts;
    }
}
