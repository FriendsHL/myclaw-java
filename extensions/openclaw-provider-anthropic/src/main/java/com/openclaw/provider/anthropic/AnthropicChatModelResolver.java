package com.openclaw.provider.anthropic;

import com.openclaw.api.config.ModelConfig;
import com.openclaw.core.agent.ChatModelResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Resolves an Anthropic-backed {@link ChatModel} from OpenClaw model configuration.
 *
 * <p>This bridges OpenClaw's {@link ModelConfig} to Spring AI's Anthropic module,
 * creating a fresh {@link AnthropicChatModel} per resolution call. In production
 * you might want to cache instances keyed by (apiKey, baseUrl, modelName).
 */
public class AnthropicChatModelResolver implements ChatModelResolver {

    private static final Logger log = LoggerFactory.getLogger(AnthropicChatModelResolver.class);

    private static final String PROVIDER_ID = "anthropic";

    @Override
    public ChatModel resolve(String modelId, ModelConfig modelConfig) {
        if (!PROVIDER_ID.equalsIgnoreCase(modelConfig.getProvider())) {
            throw new IllegalArgumentException(
                    "AnthropicChatModelResolver only supports provider 'anthropic', got: "
                    + modelConfig.getProvider());
        }

        String apiKey = modelConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "API key is required for Anthropic provider (modelId=" + modelId + ")");
        }

        String modelName = modelConfig.getModelName();
        if (modelName == null || modelName.isBlank()) {
            modelName = modelId; // fall back to the config key as the model name
        }

        // Build the low-level Anthropic API client
        String baseUrl = modelConfig.getBaseUrl();
        AnthropicApi.Builder apiBuilder = AnthropicApi.builder().apiKey(apiKey);
        if (baseUrl != null && !baseUrl.isBlank()) {
            apiBuilder.baseUrl(baseUrl);
        }
        AnthropicApi anthropicApi = apiBuilder.build();

        // Build chat options with the target model name
        AnthropicChatOptions chatOptions = AnthropicChatOptions.builder()
                .model(modelName)
                .build();

        log.info("Creating Anthropic ChatModel for modelId='{}', model='{}', baseUrl='{}'",
                modelId, modelName, baseUrl);

        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .defaultOptions(chatOptions)
                .build();
    }
}
