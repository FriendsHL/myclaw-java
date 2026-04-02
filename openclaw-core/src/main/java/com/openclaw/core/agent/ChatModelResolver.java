package com.openclaw.core.agent;

import com.openclaw.api.config.ModelConfig;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Resolves a Spring AI ChatModel instance based on model configuration.
 * This is the central bridge between OpenClaw config and Spring AI.
 *
 * <p>Implementations look up provider-specific ChatModel beans
 * (e.g. OpenAiChatModel, AnthropicChatModel) from the Spring context
 * and configure them according to the ModelConfig.
 */
public interface ChatModelResolver {

    /**
     * Resolve the ChatModel for a given model ID.
     *
     * @param modelId the model key from config (e.g. "gpt-4o")
     * @param modelConfig the model configuration (provider, apiKey, etc.)
     * @return a configured Spring AI ChatModel
     * @throws IllegalArgumentException if the provider is not supported
     */
    ChatModel resolve(String modelId, ModelConfig modelConfig);
}
