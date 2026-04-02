package com.openclaw.channel.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages tenant_access_token lifecycle for a Feishu app.
 * <p>
 * The token is obtained via {@code POST /open-apis/auth/v3/tenant_access_token/internal}
 * and expires after 2 hours. This manager refreshes proactively before expiry.
 */
public class FeishuTokenManager {

    private static final Logger LOG = Logger.getLogger(FeishuTokenManager.class.getName());
    private static final String TOKEN_URL = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * Refresh 5 minutes before expiry to avoid using an expired token.
     */
    private static final long REFRESH_MARGIN_SECONDS = 300;

    private final String appId;
    private final String appSecret;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<String> currentToken = new AtomicReference<>();

    private volatile ScheduledFuture<?> refreshTask;

    public FeishuTokenManager(String appId, String appSecret, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "feishu-token-refresh");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Obtain the initial token and start the background refresh loop.
     *
     * @throws IOException if the initial token request fails
     */
    public void start() throws IOException {
        refreshToken();
    }

    /**
     * Stop the background refresh and release resources.
     */
    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
        scheduler.shutdownNow();
        currentToken.set(null);
    }

    /**
     * Get the current valid tenant_access_token.
     *
     * @return the token, or null if not yet obtained
     */
    public String getToken() {
        return currentToken.get();
    }

    /**
     * Force a token refresh (e.g. after a 401 response).
     */
    public void forceRefresh() {
        try {
            refreshToken();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to force-refresh Feishu token", e);
        }
    }

    private void refreshToken() throws IOException {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("app_id", appId, "app_secret", appSecret));

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(RequestBody.create(body, JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Failed to obtain Feishu tenant_access_token: HTTP " + response.code());
            }

            JsonNode json = objectMapper.readTree(response.body().string());
            int code = json.path("code").asInt(-1);
            if (code != 0) {
                throw new IOException("Feishu token API error: code=" + code + ", msg=" + json.path("msg").asText());
            }

            String token = json.path("tenant_access_token").asText();
            int expire = json.path("expire").asInt(7200);

            currentToken.set(token);
            LOG.info("Feishu tenant_access_token obtained, expires in " + expire + "s");

            // Schedule next refresh before expiry
            scheduleNextRefresh(expire);
        }
    }

    private void scheduleNextRefresh(int expireSeconds) {
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }

        long delaySeconds = Math.max(expireSeconds - REFRESH_MARGIN_SECONDS, 60);
        refreshTask = scheduler.schedule(() -> {
            try {
                refreshToken();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Scheduled Feishu token refresh failed, retrying in 60s", e);
                // Retry after a shorter interval on failure
                scheduleNextRefresh(60 + (int) REFRESH_MARGIN_SECONDS);
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }
}
