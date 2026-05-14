package com.todo;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        String userMessage = (String) body.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No message provided."));
        }

        // Use your custom local AIProcessor
        AIProcessor localBrain = new AIProcessor();
        String aiResult = localBrain.processTaskWithAI(userMessage);

        // This returns the JSON your bridge.py created: {"intent": "create", "task": "..."}
        return ResponseEntity.ok(Map.of("result", aiResult));
    }
}
