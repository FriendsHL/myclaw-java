package com.openclaw.channel.feishu;

import com.openclaw.api.channel.ChannelConfigAdapter;
import com.openclaw.api.config.ChannelConfig;
import com.openclaw.api.config.OpenClawConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Resolves Feishu account configurations from the global OpenClaw config.
 * Expects config structure:
 * <pre>
 * openclaw:
 *   channels:
 *     feishu:
 *       accounts:
 *         mybot:
 *           appId: "cli_xxx"
 *           appSecret: "xxx"
 *           verificationToken: "xxx"
 *           encryptKey: "xxx"     # optional
 * </pre>
 */
public class FeishuConfigAdapter implements ChannelConfigAdapter {

    private static final String CHANNEL_ID = "feishu";

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
        Object appId = accountConfig.get("appId");
        Object appSecret = accountConfig.get("appSecret");
        return appId instanceof String && !((String) appId).isEmpty()
                && appSecret instanceof String && !((String) appSecret).isEmpty();
    }
}
