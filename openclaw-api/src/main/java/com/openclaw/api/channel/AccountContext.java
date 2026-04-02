package com.openclaw.api.channel;

import com.openclaw.api.config.OpenClawConfig;

import java.util.Map;

/**
 * Context for a specific channel account during connect/disconnect.
 */
public class AccountContext {

    private final String channelId;
    private final String accountId;
    private final OpenClawConfig config;
    private final Map<String, Object> accountConfig;
    private final InboundMessageListener messageListener;

    public AccountContext(String channelId, String accountId, OpenClawConfig config,
                          Map<String, Object> accountConfig, InboundMessageListener messageListener) {
        this.channelId = channelId;
        this.accountId = accountId;
        this.config = config;
        this.accountConfig = accountConfig;
        this.messageListener = messageListener;
    }

    public String getChannelId() { return channelId; }
    public String getAccountId() { return accountId; }
    public OpenClawConfig getConfig() { return config; }

    /** Raw account config map (channel-specific keys like token, allowFrom, etc.) */
    public Map<String, Object> getAccountConfig() { return accountConfig; }

    /** Listener to call when an inbound message is received */
    public InboundMessageListener getMessageListener() { return messageListener; }
}
