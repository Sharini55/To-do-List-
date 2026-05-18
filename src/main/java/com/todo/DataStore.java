package com.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;

@Component
public class DataStore {

    private static final String DEFAULT_DATA_DIR = "/home/data";
    private static final String DATA_FILE_NAME = "appdata.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path dataFile;

    public DataStore() {
        this.dataFile = resolveDataDir().resolve(DATA_FILE_NAME);
    }

    private Path resolveDataDir() {
        String configuredDir = System.getenv("TODO_DATA_DIR");
        Path preferred = Paths.get(configuredDir == null || configuredDir.isBlank() ? DEFAULT_DATA_DIR : configuredDir);
        try {
            Files.createDirectories(preferred);
            if (Files.isWritable(preferred)) {
                return preferred;
            }
        } catch (Exception e) {
            System.err.println("Could not create data dir: " + e.getMessage());
        }

        Path fallback = Paths.get(System.getProperty("java.io.tmpdir"), "todoapp-data");
        try {
            Files.createDirectories(fallback);
        } catch (Exception e) {
            System.err.println("Could not create fallback data dir: " + e.getMessage());
        }
        return fallback;
    }

    public String load() {
        try {
            File f = dataFile.toFile();
            if (!f.exists()) return "{}";
            return new String(Files.readAllBytes(f.toPath()));
        } catch (Exception e) {
            System.err.println("Load error: " + e.getMessage());
            return "{}";
        }
    }

    public void save(String json) {
        try {
            // Validate it's real JSON before saving
            mapper.readTree(json);
            Files.write(dataFile, json.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.err.println("Save error: " + e.getMessage());
        }
    }
}
