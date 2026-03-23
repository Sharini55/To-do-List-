package com.todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/appdata")
public class AppDataController {

    @Autowired
    private DataStore dataStore;

    @GetMapping
    public ResponseEntity<String> load() {
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(dataStore.load());
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody String json) {
        dataStore.save(json);
        return ResponseEntity.ok(Map.of("status", "saved"));
    }
}
