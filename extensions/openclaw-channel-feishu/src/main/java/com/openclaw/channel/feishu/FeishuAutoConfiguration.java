package com.openclaw.channel.feishu;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the Feishu channel plugin.
 * Only activates when OkHttp (used for Feishu API calls) is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(name = "okhttp3.OkHttpClient")
public class FeishuAutoConfiguration {

    @Bean
    public FeishuChannelPlugin feishuChannelPlugin() {
        return new FeishuChannelPlugin();
    }

    @Bean
    public FeishuEventHandler feishuEventHandler(FeishuChannelPlugin plugin) {
        return ((FeishuLifecycle) plugin.lifecycle()).getEventHandler();
    }

    @Bean
    public FeishuWebhookController feishuWebhookController(FeishuEventHandler eventHandler) {
        return new FeishuWebhookController(eventHandler);
    }
}
