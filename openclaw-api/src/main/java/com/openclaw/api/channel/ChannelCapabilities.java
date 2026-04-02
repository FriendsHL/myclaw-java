package com.openclaw.api.channel;

/**
 * Declares what features a channel supports.
 * Used by the gateway to adapt behavior per channel.
 */
public class ChannelCapabilities {

    private final boolean supportsGroups;
    private final boolean supportsThreads;
    private final boolean supportsReactions;
    private final boolean supportsMedia;
    private final boolean supportsVoice;
    private final boolean supportsEditing;

    private ChannelCapabilities(Builder builder) {
        this.supportsGroups = builder.supportsGroups;
        this.supportsThreads = builder.supportsThreads;
        this.supportsReactions = builder.supportsReactions;
        this.supportsMedia = builder.supportsMedia;
        this.supportsVoice = builder.supportsVoice;
        this.supportsEditing = builder.supportsEditing;
    }

    public boolean supportsGroups() { return supportsGroups; }
    public boolean supportsThreads() { return supportsThreads; }
    public boolean supportsReactions() { return supportsReactions; }
    public boolean supportsMedia() { return supportsMedia; }
    public boolean supportsVoice() { return supportsVoice; }
    public boolean supportsEditing() { return supportsEditing; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean supportsGroups;
        private boolean supportsThreads;
        private boolean supportsReactions;
        private boolean supportsMedia;
        private boolean supportsVoice;
        private boolean supportsEditing;

        public Builder supportsGroups(boolean v) { this.supportsGroups = v; return this; }
        public Builder supportsThreads(boolean v) { this.supportsThreads = v; return this; }
        public Builder supportsReactions(boolean v) { this.supportsReactions = v; return this; }
        public Builder supportsMedia(boolean v) { this.supportsMedia = v; return this; }
        public Builder supportsVoice(boolean v) { this.supportsVoice = v; return this; }
        public Builder supportsEditing(boolean v) { this.supportsEditing = v; return this; }

        public ChannelCapabilities build() { return new ChannelCapabilities(this); }
    }
}
