package com.openclaw.provider.openai;

import com.openclaw.core.agent.ChatModelResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration that registers the OpenAI
 * {@link ChatModelResolver} implementation.
 *
 * <p>Because {@code GatewayAutoConfiguration} declares its default
 * {@code ChatModelResolver} bean with {@code @ConditionalOnMissingBean},
 * this bean takes precedence when the OpenAI provider module is on the classpath.
 */
@AutoConfiguration
public class OpenAiProviderAutoConfiguration {

    @Bean
    public ChatModelResolver openAiChatModelResolver() {
        return new OpenAiChatModelResolver();
    }
}
