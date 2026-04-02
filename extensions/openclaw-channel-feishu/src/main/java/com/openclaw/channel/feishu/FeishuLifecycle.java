package com.openclaw.channel.feishu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.api.channel.AccountContext;
import com.openclaw.api.channel.ChannelLifecycle;
import okhttp3.OkHttpClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Manages Feishu bot lifecycle per account.
 * <p>
 * On connect:
 * <ol>
 *   <li>Obtain tenant_access_token via Feishu API</li>
 *   <li>Start background token refresh</li>
 *   <li>Register the account in the event handler and outbound client map</li>
 * </ol>
 * <p>
 * The webhook endpoint is exposed via {@link FeishuWebhookController},
 * which delegates to {@link FeishuEventHandler} for event dispatch.
 */
public class FeishuLifecycle implements ChannelLifecycle {

    private static final Logger LOG = Logger.getLogger(FeishuLifecycle.class.getName());

    private final FeishuOutbound outbound;
    private final FeishuEventHandler eventHandler;
    private final ConcurrentMap<String, FeishuTokenManager> tokenManagers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, OkHttpClient> httpClients = new ConcurrentHashMap<>();

    public FeishuLifecycle(FeishuOutbound outbound) {
        this.outbound = outbound;
        this.eventHandler = new FeishuEventHandler();
    }

    /** Expose the event handler for the webhook controller. */
    public FeishuEventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public void connect(AccountContext ctx) throws Exception {
        String appId = (String) ctx.getAccountConfig().get("appId");
        String appSecret = (String) ctx.getAccountConfig().get("appSecret");

        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            throw new IllegalStateException(
                    "Feishu appId and appSecret are required for account: " + ctx.getAccountId());
        }

        // Create a shared OkHttpClient for this account
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        httpClients.put(ctx.getAccountId(), httpClient);

        ObjectMapper objectMapper = new ObjectMapper();

        // Start the token manager (obtains initial token + starts refresh loop)
        FeishuTokenManager tokenManager = new FeishuTokenManager(appId, appSecret, httpClient, objectMapper);
        tokenManager.start();
        tokenManagers.put(ctx.getAccountId(), tokenManager);

        // Register outbound client
        outbound.registerClient(ctx.getAccountId(), httpClient, tokenManager);

        // Register account context in event handler for inbound dispatch
        eventHandler.registerAccount(ctx.getAccountId(), ctx);

        LOG.info("Feishu channel connected for account: " + ctx.getAccountId());
    }

    @Override
    public void disconnect(AccountContext ctx) throws Exception {
        // Stop token refresh
        FeishuTokenManager tokenManager = tokenManagers.remove(ctx.getAccountId());
        if (tokenManager != null) {
            tokenManager.stop();
        }

        // Remove outbound client
        outbound.removeClient(ctx.getAccountId());

        // Remove event handler registration
        eventHandler.removeAccount(ctx.getAccountId());

        // Shutdown HTTP client dispatcher
        OkHttpClient httpClient = httpClients.remove(ctx.getAccountId());
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }

        LOG.info("Feishu channel disconnected for account: " + ctx.getAccountId());
    }
}
