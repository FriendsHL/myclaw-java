package com.openclaw.channel.feishu;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring REST controller that exposes the Feishu event webhook endpoint.
 * <p>
 * Feishu pushes event callbacks (message received, URL verification, etc.)
 * to {@code POST /api/channels/feishu/{accountId}/webhook}.
 * <p>
 * This controller delegates to {@link FeishuEventHandler} for actual processing.
 */
@RestController
@RequestMapping("/api/channels/feishu")
public class FeishuWebhookController {

    private final FeishuEventHandler eventHandler;

    public FeishuWebhookController(FeishuEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Handle incoming Feishu event callbacks.
     *
     * @param accountId the Feishu account ID (from URL path)
     * @param body      raw JSON event payload
     * @return JSON response (challenge for verification, or acknowledgment)
     */
    @PostMapping(value = "/{accountId}/webhook",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWebhook(
            @PathVariable String accountId,
            @RequestBody String body) {

        String response = eventHandler.handleEvent(accountId, body);
        return ResponseEntity.ok(response);
    }
}
