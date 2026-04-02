package com.openclaw.channel.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.api.channel.ChannelOutbound;
import com.openclaw.api.channel.OutboundContext;
import com.openclaw.api.message.DeliveryResult;
import com.openclaw.api.message.OutboundPayload;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends outbound messages to Feishu chats via the IM API.
 * Uses {@code POST /open-apis/im/v1/messages?receive_id_type=chat_id} with Bearer token.
 */
public class FeishuOutbound implements ChannelOutbound {

    private static final Logger LOG = Logger.getLogger(FeishuOutbound.class.getName());
    private static final String SEND_MESSAGE_URL = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final ConcurrentMap<String, AccountClient> clients = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Holds the HTTP client and token manager for one account. */
    static class AccountClient {
        final OkHttpClient httpClient;
        final FeishuTokenManager tokenManager;

        AccountClient(OkHttpClient httpClient, FeishuTokenManager tokenManager) {
            this.httpClient = httpClient;
            this.tokenManager = tokenManager;
        }
    }

    /**
     * Register an HTTP client and token manager for a given account.
     */
    public void registerClient(String accountId, OkHttpClient httpClient, FeishuTokenManager tokenManager) {
        clients.put(accountId, new AccountClient(httpClient, tokenManager));
    }

    /**
     * Remove the client for an account on disconnect.
     */
    public void removeClient(String accountId) {
        clients.remove(accountId);
    }

    @Override
    public DeliveryResult send(OutboundContext context, OutboundPayload payload) {
        AccountClient client = clients.get(context.getAccountId());
        if (client == null) {
            return DeliveryResult.failure("No Feishu client registered for account: " + context.getAccountId());
        }

        String token = client.tokenManager.getToken();
        if (token == null) {
            return DeliveryResult.failure("Feishu token not available for account: " + context.getAccountId());
        }

        try {
            // Build the message content JSON: {"text":"..."}
            String contentJson = objectMapper.writeValueAsString(Map.of("text", payload.getText()));

            // Build the request body
            String body = objectMapper.writeValueAsString(Map.of(
                    "receive_id", context.getPeerId(),
                    "msg_type", "text",
                    "content", contentJson
            ));

            Request request = new Request.Builder()
                    .url(SEND_MESSAGE_URL)
                    .header("Authorization", "Bearer " + token)
                    .post(RequestBody.create(body, JSON_MEDIA_TYPE))
                    .build();

            try (Response response = client.httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    return DeliveryResult.failure("Empty response from Feishu send API");
                }

                JsonNode json = objectMapper.readTree(response.body().string());
                int code = json.path("code").asInt(-1);
                if (code != 0) {
                    String msg = json.path("msg").asText("unknown error");
                    LOG.log(Level.WARNING, "Feishu send message failed: code={0}, msg={1}",
                            new Object[]{code, msg});
                    return DeliveryResult.failure("Feishu API error: " + msg);
                }

                String messageId = json.path("data").path("message_id").asText(null);
                return DeliveryResult.success(messageId);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send Feishu message", e);
            return DeliveryResult.failure("Failed to send Feishu message: " + e.getMessage());
        }
    }
}
