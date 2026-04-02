package com.openclaw.api.event;

import java.time.Instant;

/**
 * Base class for all gateway events.
 * Published via Spring ApplicationEventPublisher.
 */
public abstract class GatewayEvent {

    private final Instant timestamp;

    protected GatewayEvent() {
        this.timestamp = Instant.now();
    }

    public Instant getTimestamp() { return timestamp; }
}
