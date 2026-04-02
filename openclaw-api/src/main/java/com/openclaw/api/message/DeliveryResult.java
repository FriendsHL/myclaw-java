package com.openclaw.api.message;

/**
 * Result of sending a message to a channel platform.
 */
public class DeliveryResult {

    private final boolean success;
    private final String platformMessageId;
    private final String errorMessage;

    private DeliveryResult(boolean success, String platformMessageId, String errorMessage) {
        this.success = success;
        this.platformMessageId = platformMessageId;
        this.errorMessage = errorMessage;
    }

    public static DeliveryResult success(String platformMessageId) {
        return new DeliveryResult(true, platformMessageId, null);
    }

    public static DeliveryResult failure(String errorMessage) {
        return new DeliveryResult(false, null, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getPlatformMessageId() { return platformMessageId; }
    public String getErrorMessage() { return errorMessage; }
}
