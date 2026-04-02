package com.openclaw.channel.telegram;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the Telegram channel plugin.
 * Only activates when the telegrambots long-polling library is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication")
public class TelegramAutoConfiguration {

    @Bean
    public TelegramChannelPlugin telegramChannelPlugin() {
        return new TelegramChannelPlugin();
    }
}
