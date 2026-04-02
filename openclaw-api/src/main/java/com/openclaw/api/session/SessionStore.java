package com.openclaw.api.session;

import java.util.List;

/**
 * Persistence interface for session transcripts.
 * Implementations can store to files, databases, etc.
 */
public interface SessionStore {

    /** Load the transcript for a session key. Returns empty list if not found. */
    List<SessionEntry> load(SessionKey key);

    /** Append an entry to the session transcript */
    void append(SessionKey key, SessionEntry entry);

    /** Replace the entire transcript for a session */
    void save(SessionKey key, List<SessionEntry> entries);

    /** Check if a session exists */
    boolean exists(SessionKey key);

    /** Delete a session */
    void delete(SessionKey key);
}
