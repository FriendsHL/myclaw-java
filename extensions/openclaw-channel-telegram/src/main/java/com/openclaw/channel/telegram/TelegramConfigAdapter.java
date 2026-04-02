package com.openclaw.channel.telegram;

import com.openclaw.api.channel.ChannelConfigAdapter;
import com.openclaw.api.config.ChannelConfig;
import com.openclaw.api.config.OpenClawConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Resolves Telegram account configurations from the global OpenClaw config.
 * Expects config structure:
 * <pre>
 * openclaw:
 *   channels:
 *     telegram:
 *       accounts:
 *         mybot:
 *           token: "123456:ABC-DEF..."
 * </pre>
 */
public class TelegramConfigAdapter implements ChannelConfigAdapter {

    private static final String CHANNEL_ID = "telegram";

    @Override
    public List<String> listAccountIds(OpenClawConfig config) {
        ChannelConfig channelConfig = config.getChannels().get(CHANNEL_ID);
        if (channelConfig == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(channelConfig.getAccounts().keySet());
    }

    @Override
    public Map<String, Object> resolveAccountConfig(OpenClawConfig config, String accountId) {
        ChannelConfig channelConfig = config.getChannels().get(CHANNEL_ID);
        if (channelConfig == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> account = channelConfig.getAccounts().get(accountId);
        return account != null ? account : Collections.emptyMap();
    }

    @Override
    public boolean isConfigured(Map<String, Object> accountConfig) {
        Object token = accountConfig.get("token");
        return token instanceof String && !((String) token).isEmpty();
    }
}
