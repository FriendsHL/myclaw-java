package com.openclaw.provider.openai;

import com.openclaw.api.config.ModelConfig;
import com.openclaw.core.agent.ChatModelResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * Resolves an OpenAI-backed {@link ChatModel} from OpenClaw model configuration.
 *
 * <p>This bridges OpenClaw's {@link ModelConfig} to Spring AI's OpenAI module,
 * creating a fresh {@link OpenAiChatModel} per resolution call. In production
 * you might want to cache instances keyed by (apiKey, baseUrl, modelName).
 */
public class OpenAiChatModelResolver implements ChatModelResolver {

    private static final Logger log = LoggerFactory.getLogger(OpenAiChatModelResolver.class);

    private static final String PROVIDER_ID = "openai";

    @Override
    public ChatModel resolve(String modelId, ModelConfig modelConfig) {
        if (!PROVIDER_ID.equalsIgnoreCase(modelConfig.getProvider())) {
            throw new IllegalArgumentException(
                    "OpenAiChatModelResolver only supports provider 'openai', got: "
                    + modelConfig.getProvider());
        }

        String apiKey = modelConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "API key is required for OpenAI provider (modelId=" + modelId + ")");
        }

        String modelName = modelConfig.getModelName();
        if (modelName == null || modelName.isBlank()) {
            modelName = modelId; // fall back to the config key as the model name
        }

        // Build the low-level OpenAI API client
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder().apiKey(apiKey);
        String baseUrl = modelConfig.getBaseUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            apiBuilder.baseUrl(baseUrl);
        }
        OpenAiApi openAiApi = apiBuilder.build();

        // Build chat options with the target model name
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(modelName)
                .build();

        log.info("Creating OpenAI ChatModel for modelId='{}', model='{}', baseUrl='{}'",
                modelId, modelName, baseUrl);

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(chatOptions)
                .build();
    }
}
