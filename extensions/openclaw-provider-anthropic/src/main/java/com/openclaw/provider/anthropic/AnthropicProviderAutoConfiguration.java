package com.openclaw.provider.anthropic;

import com.openclaw.core.agent.ChatModelResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration that registers the Anthropic
 * {@link ChatModelResolver} implementation.
 *
 * <p>Because {@code GatewayAutoConfiguration} declares its default
 * {@code ChatModelResolver} bean with {@code @ConditionalOnMissingBean},
 * this bean takes precedence when the Anthropic provider module is on the classpath.
 */
@AutoConfiguration
public class AnthropicProviderAutoConfiguration {

    @Bean
    public ChatModelResolver anthropicChatModelResolver() {
        return new AnthropicChatModelResolver();
    }
}
