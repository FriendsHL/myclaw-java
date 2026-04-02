package com.openclaw.core.routing;

import com.openclaw.api.config.ChannelConfig;
import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.api.message.InboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Checks whether an inbound message sender is allowed.
 * Looks up the channel/account's allow-from config.
 */
public class AllowlistChecker {

    private static final Logger log = LoggerFactory.getLogger(AllowlistChecker.class);

    private volatile OpenClawConfig config;

    public AllowlistChecker(OpenClawConfig config) {
        this.config = config;
    }

    public void updateConfig(OpenClawConfig config) {
        this.config = config;
    }

    /**
     * Check if the sender of this message is allowed.
     * Returns true if allowed (or if no allowlist is configured).
     */
    public boolean isAllowed(InboundMessage message) {
        ChannelConfig channelConfig = config.getChannels().get(message.getChannelId());
        if (channelConfig == null) return true;

        Map<String, Object> accountConfig = channelConfig.getAccounts().get(message.getAccountId());
        if (accountConfig == null) return true;

        Object allowFromObj = accountConfig.get("allow-from");
        if (allowFromObj == null) {
            allowFromObj = accountConfig.get("allowFrom");
        }
        if (!(allowFromObj instanceof List<?>)) return true;

        @SuppressWarnings("unchecked")
        List<String> allowFrom = ((List<?>) allowFromObj).stream()
                .map(Object::toString)
                .toList();

        if (allowFrom.isEmpty()) return true;

        boolean allowed = allowFrom.contains(message.getSenderId())
                || allowFrom.contains(message.getPeerId());

        if (!allowed) {
            log.debug("Sender {} not in allowlist for {}/{}",
                    message.getSenderId(), message.getChannelId(), message.getAccountId());
        }
        return allowed;
    }
}
