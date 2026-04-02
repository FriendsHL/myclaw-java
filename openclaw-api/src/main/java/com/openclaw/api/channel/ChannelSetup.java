package com.openclaw.api.channel;

import java.util.Map;

/**
 * Interactive setup wizard for configuring a channel account.
 * Optional - channels that don't need interactive setup can skip this.
 */
public interface ChannelSetup {

    /** Human-readable setup instructions */
    String instructions();

    /**
     * Validate the provided config values.
     * @return null if valid, or an error message describing the issue
     */
    String validate(Map<String, String> inputs);
}
