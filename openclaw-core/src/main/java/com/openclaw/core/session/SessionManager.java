package com.openclaw.core.session;

import com.openclaw.api.session.SessionEntry;
import com.openclaw.api.session.SessionKey;
import com.openclaw.api.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Manages conversation sessions: loading history, appending messages.
 */
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final SessionStore store;
    private final int maxHistorySize;

    public SessionManager(SessionStore store, int maxHistorySize) {
        this.store = store;
        this.maxHistorySize = maxHistorySize;
    }

    public SessionManager(SessionStore store) {
        this(store, 50);
    }

    /** Load conversation history for a session */
    public List<SessionEntry> getHistory(SessionKey key) {
        List<SessionEntry> entries = store.load(key);
        // Trim to recent history if too long
        if (entries.size() > maxHistorySize) {
            return entries.subList(entries.size() - maxHistorySize, entries.size());
        }
        return entries;
    }

    /** Append a user message to the session */
    public void appendUserMessage(SessionKey key, String content) {
        store.append(key, new SessionEntry(SessionEntry.Role.USER, content));
    }

    /** Append an assistant response to the session */
    public void appendAssistantMessage(SessionKey key, String content) {
        store.append(key, new SessionEntry(SessionEntry.Role.ASSISTANT, content));
    }

    /** Check if a session has prior history */
    public boolean hasHistory(SessionKey key) {
        return store.exists(key);
    }

    /** Clear session history */
    public void clearHistory(SessionKey key) {
        store.delete(key);
    }
}
