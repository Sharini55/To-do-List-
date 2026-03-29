package com.todo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Gemini API key not configured. Add GEMINI_API_KEY to your Azure app settings."));
        }

        String userMessage = (String) body.get("message");
        String context = (String) body.getOrDefault("context", "");

        // Build the prompt
        String systemPrompt = """
You are an AI assistant for a productivity app. The user's current data is provided as context.
You MUST respond with ONLY valid JSON — no markdown, no explanation, no backticks.

The JSON must have this structure:
{
  "action": "<action_name>",
  "params": { ... },
  "reply": "<friendly confirmation message to show the user>"
}

Available actions:
- "add_task": params: { title, dueDate (YYYY-MM-DD), priority ("Red"|"Yellow"|"Green"|"None"), description }
- "add_event": params: { title, date (YYYY-MM-DD), startTime (HH:MM), endTime (HH:MM), location, color, repeat ("none"|"daily"|"weekly"|"biweekly"|"monthly") }
- "add_reminder": params: { title, datetime (YYYY-MM-DDTHH:MM) }
- "add_habit": params: { name, icon, duration (number of days) }
- "delete_task": params: { title } (match by title substring)
- "delete_event": params: { title } (match by title substring)
- "reschedule_task": params: { title, newDate (YYYY-MM-DD) }
- "reschedule_event": params: { title, newDate (YYYY-MM-DD), newStartTime (HH:MM, optional) }
- "complete_task": params: { title }
- "query_schedule": params: { date (YYYY-MM-DD) } — returns tasks, events, reminders for that date
- "show_report": params: {} — triggers the report/analytics view
- "none": params: {} — for greetings or questions you answer directly in reply

Today's date is """ + java.time.LocalDate.now() + """
. When the user says "tomorrow", use """ + java.time.LocalDate.now().plusDays(1) + """
. "next Friday" means the coming Friday. Infer missing info smartly.

Priority mapping: "high"→"Red", "medium"→"Yellow", "low"→"Green".
Default priority: "None". Default event duration: 1 hour.

User's current app data (for context when answering questions):
""" + context;

        // Build Gemini API request
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", systemPrompt + "\n\nUser message: " + userMessage)
                })
            },
            "generationConfig", Map.of(
                "temperature", 0.1,
                "maxOutputTokens", 512
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            var candidates = (java.util.List<?>) response.getBody().get("candidates");
            var content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            var parts = (java.util.List<?>) content.get("parts");
            String text = (String) ((Map<?, ?>) parts.get(0)).get("text");

            // Clean up any accidental markdown wrapping
            text = text.strip();
            if (text.startsWith("```")) {
                text = text.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
            }

            return ResponseEntity.ok(Map.of("result", text));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Gemini request failed: " + e.getMessage()));
        }
    }
}
