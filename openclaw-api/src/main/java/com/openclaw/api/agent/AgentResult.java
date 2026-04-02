package com.openclaw.api.agent;

import com.openclaw.api.message.OutboundPayload;

/**
 * Result from an agent processing a message.
 */
public class AgentResult {

    private final String responseText;
    private final OutboundPayload outboundPayload;
    private final boolean success;
    private final String errorMessage;

    private AgentResult(String responseText, OutboundPayload outboundPayload,
                        boolean success, String errorMessage) {
        this.responseText = responseText;
        this.outboundPayload = outboundPayload;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static AgentResult success(String responseText) {
        return new AgentResult(responseText, new OutboundPayload(responseText), true, null);
    }

    public static AgentResult failure(String errorMessage) {
        return new AgentResult(null, null, false, errorMessage);
    }

    public String getResponseText() { return responseText; }
    public OutboundPayload getOutboundPayload() { return outboundPayload; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
