package com.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;

@Component
public class DataStore {

    private static final String DATA_DIR = "/home/data";
    private static final String DATA_FILE = DATA_DIR + "/appdata.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public DataStore() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (Exception e) {
            System.err.println("Could not create data dir: " + e.getMessage());
        }
    }

    public String load() {
        try {
            File f = new File(DATA_FILE);
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
            Files.write(Paths.get(DATA_FILE), json.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.err.println("Save error: " + e.getMessage());
        }
    }
}
