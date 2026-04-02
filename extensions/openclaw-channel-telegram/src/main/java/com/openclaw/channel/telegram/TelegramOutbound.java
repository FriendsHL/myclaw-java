package com.openclaw.channel.telegram;

import com.openclaw.api.channel.ChannelOutbound;
import com.openclaw.api.channel.OutboundContext;
import com.openclaw.api.message.DeliveryResult;
import com.openclaw.api.message.OutboundPayload;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sends outbound messages to Telegram chats via the Bot API.
 * Maintains a per-account TelegramClient cache keyed by bot token.
 */
public class TelegramOutbound implements ChannelOutbound {

    private final ConcurrentMap<String, TelegramClient> clients = new ConcurrentHashMap<>();

    /**
     * Register a TelegramClient for a given account so outbound sends can use it.
     */
    public void registerClient(String accountId, TelegramClient client) {
        clients.put(accountId, client);
    }

    /**
     * Remove the client for an account on disconnect.
     */
    public void removeClient(String accountId) {
        clients.remove(accountId);
    }

    @Override
    public DeliveryResult send(OutboundContext context, OutboundPayload payload) {
        TelegramClient client = clients.get(context.getAccountId());
        if (client == null) {
            return DeliveryResult.failure("No Telegram client registered for account: " + context.getAccountId());
        }
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(context.getPeerId())
                    .text(payload.getText())
                    .build();
            Message result = client.execute(sendMessage);
            return DeliveryResult.success(String.valueOf(result.getMessageId()));
        } catch (Exception e) {
            return DeliveryResult.failure("Failed to send Telegram message: " + e.getMessage());
        }
    }
}
