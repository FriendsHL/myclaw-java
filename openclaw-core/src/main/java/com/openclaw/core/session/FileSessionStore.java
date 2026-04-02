package com.openclaw.core.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openclaw.api.session.SessionEntry;
import com.openclaw.api.session.SessionKey;
import com.openclaw.api.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File-based session store.
 * Each session is stored as a JSON file under the sessions directory.
 */
public class FileSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(FileSessionStore.class);

    private final Path sessionsDir;
    private final ObjectMapper mapper;

    public FileSessionStore(Path sessionsDir) {
        this.sessionsDir = sessionsDir;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ensureDirectory();
    }

    public FileSessionStore() {
        this(Path.of(System.getProperty("user.home"), ".openclaw", "sessions"));
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(sessionsDir);
        } catch (IOException e) {
            log.error("Failed to create sessions directory: {}", sessionsDir, e);
        }
    }

    private Path sessionFile(SessionKey key) {
        // Sanitize key for safe file names
        String fileName = key.toKeyString()
                .replace(":", "_")
                .replace("/", "_")
                + ".json";
        return sessionsDir.resolve(fileName);
    }

    @Override
    public List<SessionEntry> load(SessionKey key) {
        Path file = sessionFile(key);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file.toFile(), new TypeReference<List<SessionEntry>>() {});
        } catch (IOException e) {
            log.error("Failed to load session: {}", key, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void append(SessionKey key, SessionEntry entry) {
        List<SessionEntry> entries = load(key);
        entries.add(entry);
        save(key, entries);
    }

    @Override
    public void save(SessionKey key, List<SessionEntry> entries) {
        Path file = sessionFile(key);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), entries);
        } catch (IOException e) {
            log.error("Failed to save session: {}", key, e);
        }
    }

    @Override
    public boolean exists(SessionKey key) {
        return Files.exists(sessionFile(key));
    }

    @Override
    public void delete(SessionKey key) {
        try {
            Files.deleteIfExists(sessionFile(key));
        } catch (IOException e) {
            log.error("Failed to delete session: {}", key, e);
        }
    }
}
