package com.openclaw.core.agent;

import com.openclaw.api.agent.AgentContext;
import com.openclaw.api.agent.AgentResult;
import com.openclaw.api.config.AgentConfig;
import com.openclaw.api.config.ModelConfig;
import com.openclaw.api.config.OpenClawConfig;
import com.openclaw.api.session.SessionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes agent logic using Spring AI ChatClient/ChatModel.
 * <p>
 * Flow:
 * 1. Load agent config (system prompt, model reference)
 * 2. Resolve ChatModel via ChatModelResolver
 * 3. Build conversation messages from session history
 * 4. Call Spring AI ChatClient
 * 5. Return AgentResult with response text
 */
public class AgentRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentRunner.class);

    private final ChatModelResolver chatModelResolver;
    private volatile OpenClawConfig config;

    public AgentRunner(ChatModelResolver chatModelResolver, OpenClawConfig config) {
        this.chatModelResolver = chatModelResolver;
        this.config = config;
    }

    public void updateConfig(OpenClawConfig config) {
        this.config = config;
    }

    /**
     * Process a message through the agent pipeline.
     */
    public AgentResult process(AgentContext context) {
        AgentConfig agentConfig = context.getAgentConfig();
        String modelId = agentConfig.getModel();

        try {
            // Resolve the Spring AI ChatModel
            ModelConfig modelConfig = config.getModels().get(modelId);
            if (modelConfig == null) {
                return AgentResult.failure("Model not configured: " + modelId);
            }

            ChatModel chatModel = chatModelResolver.resolve(modelId, modelConfig);

            // Build conversation messages from history
            List<Message> messages = buildMessages(agentConfig, context);

            // Call Spring AI
            Prompt prompt = new Prompt(messages);
            String response = ChatClient.create(chatModel)
                    .prompt(prompt)
                    .call()
                    .content();

            log.debug("Agent '{}' response: {} chars", context.getRoute().getAgentId(),
                    response != null ? response.length() : 0);

            return AgentResult.success(response != null ? response : "");

        } catch (Exception e) {
            log.error("Agent processing failed for agent '{}'", context.getRoute().getAgentId(), e);
            return AgentResult.failure("Agent error: " + e.getMessage());
        }
    }

    /**
     * Build Spring AI Message list from system prompt + session history + current message.
     */
    private List<Message> buildMessages(AgentConfig agentConfig, AgentContext context) {
        List<Message> messages = new ArrayList<>();

        // System prompt
        if (agentConfig.getSystemPrompt() != null && !agentConfig.getSystemPrompt().isEmpty()) {
            messages.add(new SystemMessage(agentConfig.getSystemPrompt()));
        }

        // Session history
        for (SessionEntry entry : context.getHistory()) {
            switch (entry.getRole()) {
                case USER -> messages.add(new UserMessage(entry.getContent()));
                case ASSISTANT -> messages.add(new AssistantMessage(entry.getContent()));
                case SYSTEM -> messages.add(new SystemMessage(entry.getContent()));
            }
        }

        // Current user message
        messages.add(new UserMessage(context.getMessage().getText()));

        return messages;
    }
}
