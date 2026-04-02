package com.openclaw.api.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * A single entry in a conversation transcript.
 * Supports Jackson deserialization via {@link JsonCreator}.
 */
public class SessionEntry {

    public enum Role { USER, ASSISTANT, SYSTEM }

    private final Role role;
    private final String content;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    /**
     * Primary constructor used by Jackson deserialization and programmatic creation.
     */
    @JsonCreator
    public SessionEntry(
            @JsonProperty("role") Role role,
            @JsonProperty("content") String content,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("metadata") Map<String, Object> metadata) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metadata = metadata;
    }

    /**
     * Convenience constructor for creating entries without explicit timestamp/metadata.
     */
    public SessionEntry(Role role, String content) {
        this(role, content, Instant.now(), null);
    }

    public Role getRole() { return role; }
    public String getContent() { return content; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getMetadata() { return metadata; }
}
