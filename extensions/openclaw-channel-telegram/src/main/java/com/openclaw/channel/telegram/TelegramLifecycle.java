package com.openclaw.channel.telegram;

import com.openclaw.api.channel.AccountContext;
import com.openclaw.api.channel.ChannelLifecycle;
import com.openclaw.api.channel.PluginContext;
import com.openclaw.api.message.ChatType;
import com.openclaw.api.message.InboundMessage;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages Telegram long-polling connections per account.
 * On connect, starts a polling application that receives Updates and
 * normalizes them into InboundMessage objects for the gateway.
 */
public class TelegramLifecycle implements ChannelLifecycle {

    private final TelegramOutbound outbound;
    private final ConcurrentMap<String, TelegramBotsLongPollingApplication> pollingApps = new ConcurrentHashMap<>();

    public TelegramLifecycle(TelegramOutbound outbound) {
        this.outbound = outbound;
    }

    @Override
    public void connect(AccountContext ctx) throws Exception {
        String token = (String) ctx.getAccountConfig().get("token");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Telegram bot token is not configured for account: " + ctx.getAccountId());
        }

        // Create and register a TelegramClient for outbound use
        TelegramClient client = new OkHttpTelegramClient(token);
        outbound.registerClient(ctx.getAccountId(), client);

        // Create the long-polling application and register an update consumer
        TelegramBotsLongPollingApplication pollingApp = new TelegramBotsLongPollingApplication();

        LongPollingSingleThreadUpdateConsumer consumer = update -> handleUpdate(update, ctx);

        pollingApp.registerBot(token, consumer);
        pollingApps.put(ctx.getAccountId(), pollingApp);
    }

    @Override
    public void disconnect(AccountContext ctx) throws Exception {
        TelegramBotsLongPollingApplication pollingApp = pollingApps.remove(ctx.getAccountId());
        if (pollingApp != null) {
            pollingApp.close();
        }
        outbound.removeClient(ctx.getAccountId());
    }

    private void handleUpdate(Update update, AccountContext ctx) {
        if (update.getMessage() == null) {
            return;
        }

        Message message = update.getMessage();
        String text = message.getText();
        if (text == null || text.isEmpty()) {
            // Skip non-text messages for now
            return;
        }

        User from = message.getFrom();
        String chatId = String.valueOf(message.getChatId());
        String senderId = from != null ? String.valueOf(from.getId()) : "unknown";
        String senderName = buildSenderName(from);

        // Determine chat type from Telegram chat type
        ChatType chatType = resolveChatType(message);

        InboundMessage inbound = InboundMessage.builder()
                .channelId("telegram")
                .accountId(ctx.getAccountId())
                .peerId(chatId)
                .chatType(chatType)
                .senderId(senderId)
                .senderName(senderName)
                .text(text)
                .timestamp(Instant.ofEpochSecond(message.getDate()))
                .build();

        ctx.getMessageListener().onMessage(inbound);
    }

    private String buildSenderName(User user) {
        if (user == null) {
            return "Unknown";
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String name = (firstName + " " + lastName).trim();
        return name.isEmpty() ? "Unknown" : name;
    }

    private ChatType resolveChatType(Message message) {
        if (message.getChat() == null) {
            return ChatType.DM;
        }
        String type = message.getChat().getType();
        if ("group".equals(type) || "supergroup".equals(type)) {
            return ChatType.GROUP;
        } else if ("channel".equals(type)) {
            return ChatType.CHANNEL;
        }
        return ChatType.DM;
    }
}
