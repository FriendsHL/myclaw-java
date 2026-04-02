package com.openclaw.channel.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.api.channel.AccountContext;
import com.openclaw.api.channel.InboundMessageListener;
import com.openclaw.api.message.ChatType;
import com.openclaw.api.message.InboundMessage;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Feishu event callbacks pushed to our webhook endpoint.
 * <p>
 * Supports:
 * <ul>
 *   <li>URL verification challenge ({@code url_verification})</li>
 *   <li>Message receive events ({@code im.message.receive_v1})</li>
 * </ul>
 */
public class FeishuEventHandler {

    private static final Logger LOG = Logger.getLogger(FeishuEventHandler.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<String, AccountContext> accountContexts = new ConcurrentHashMap<>();

    /**
     * Register an account context for event dispatching.
     */
    public void registerAccount(String accountId, AccountContext ctx) {
        accountContexts.put(accountId, ctx);
    }

    /**
     * Remove an account context.
     */
    public void removeAccount(String accountId) {
        accountContexts.remove(accountId);
    }

    /**
     * Handle a raw event JSON body from Feishu webhook.
     *
     * @param accountId the account this event belongs to
     * @param body      raw JSON string from the webhook POST
     * @return a response body to send back (for verification challenges), or null
     */
    public String handleEvent(String accountId, String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            // URL verification challenge
            String type = root.path("type").asText(null);
            if ("url_verification".equals(type)) {
                return handleUrlVerification(root);
            }

            // Event callback (v2 schema)
            JsonNode header = root.path("header");
            String eventType = header.path("event_type").asText(null);

            if ("im.message.receive_v1".equals(eventType)) {
                handleMessageReceive(accountId, root.path("event"));
            } else {
                LOG.fine("Ignoring Feishu event type: " + eventType);
            }

            return "{\"code\":0}";
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error handling Feishu event for account " + accountId, e);
            return "{\"code\":0}";
        }
    }

    /**
     * Handle URL verification challenge.
     * Feishu sends: {"challenge":"xxx","token":"xxx","type":"url_verification"}
     * We respond: {"challenge":"xxx"}
     */
    private String handleUrlVerification(JsonNode root) {
        String challenge = root.path("challenge").asText("");
        try {
            return objectMapper.writeValueAsString(Map.of("challenge", challenge));
        } catch (Exception e) {
            return "{\"challenge\":\"" + challenge + "\"}";
        }
    }

    /**
     * Handle im.message.receive_v1 event.
     * Event payload structure:
     * <pre>
     * {
     *   "sender": {
     *     "sender_id": { "open_id": "ou_xxx", "user_id": "xxx", "union_id": "xxx" },
     *     "sender_type": "user",
     *     "tenant_key": "xxx"
     *   },
     *   "message": {
     *     "message_id": "om_xxx",
     *     "chat_id": "oc_xxx",
     *     "chat_type": "p2p" | "group",
     *     "message_type": "text",
     *     "content": "{\"text\":\"hello\"}"
     *   }
     * }
     * </pre>
     */
    private void handleMessageReceive(String accountId, JsonNode event) {
        AccountContext ctx = accountContexts.get(accountId);
        if (ctx == null) {
            LOG.warning("Received Feishu event for unregistered account: " + accountId);
            return;
        }

        InboundMessageListener listener = ctx.getMessageListener();
        if (listener == null) {
            LOG.warning("No message listener registered for Feishu account: " + accountId);
            return;
        }

        JsonNode message = event.path("message");
        JsonNode sender = event.path("sender");

        String messageType = message.path("message_type").asText("");
        if (!"text".equals(messageType)) {
            // For now, only handle text messages
            LOG.fine("Ignoring non-text Feishu message of type: " + messageType);
            return;
        }

        // Parse the message content JSON
        String text = extractTextContent(message.path("content").asText(""));
        if (text == null || text.isEmpty()) {
            return;
        }

        String chatId = message.path("chat_id").asText("");
        String senderId = sender.path("sender_id").path("open_id").asText("unknown");
        String chatTypeStr = message.path("chat_type").asText("p2p");

        ChatType chatType = "group".equals(chatTypeStr) ? ChatType.GROUP : ChatType.DM;

        // Extract timestamp (create_time is in milliseconds as a string)
        Instant timestamp = parseTimestamp(message.path("create_time").asText(null));

        InboundMessage inbound = InboundMessage.builder()
                .channelId("feishu")
                .accountId(accountId)
                .peerId(chatId)
                .chatType(chatType)
                .senderId(senderId)
                .senderName(senderId) // Feishu events don't include sender name directly
                .text(text)
                .timestamp(timestamp)
                .build();

        listener.onMessage(inbound);
    }

    /**
     * Extract plain text from Feishu message content JSON.
     * Content format: {@code {"text":"actual message text"}}
     */
    private String extractTextContent(String contentJson) {
        if (contentJson == null || contentJson.isEmpty()) {
            return null;
        }
        try {
            JsonNode contentNode = objectMapper.readTree(contentJson);
            return contentNode.path("text").asText(null);
        } catch (Exception e) {
            LOG.fine("Failed to parse Feishu message content: " + contentJson);
            return null;
        }
    }

    private Instant parseTimestamp(String createTime) {
        if (createTime == null || createTime.isEmpty()) {
            return Instant.now();
        }
        try {
            long millis = Long.parseLong(createTime);
            return Instant.ofEpochMilli(millis);
        } catch (NumberFormatException e) {
            return Instant.now();
        }
    }
}
