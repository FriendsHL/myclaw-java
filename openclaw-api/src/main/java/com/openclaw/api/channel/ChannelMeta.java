package com.openclaw.api.channel;

/**
 * Human-readable metadata for a channel plugin.
 */
public class ChannelMeta {

    private final String displayName;
    private final String description;
    private final String iconUrl;

    public ChannelMeta(String displayName, String description, String iconUrl) {
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    public ChannelMeta(String displayName, String description) {
        this(displayName, description, null);
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
}
