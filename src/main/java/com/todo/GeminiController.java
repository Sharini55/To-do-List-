package com.todo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(45000);
        this.restTemplate = new RestTemplate(factory);
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "AI API key not configured."));
        }

        String userMessage = (String) body.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No message provided."));
        }

        String context = (String) body.getOrDefault("context", "");
        String today = java.time.LocalDate.now().toString();
        String tomorrow = java.time.LocalDate.now().plusDays(1).toString();

        // Very explicit prompt with a concrete example so small models follow it
        String systemPrompt =
            "You are a productivity assistant. Today is " + today + ". Tomorrow is " + tomorrow + ".\n\n" +
            "You MUST respond with ONLY a single valid JSON object. No explanation. No markdown. No extra text.\n\n" +
            "JSON format:\n" +
            "{\"action\":\"ACTION\",\"params\":{},\"reply\":\"friendly message\"}\n\n" +
            "ACTION must be exactly one of:\n" +
            "- none : for questions/greetings, answer in reply field\n" +
            "- add_task : params: title(required), dueDate(YYYY-MM-DD), priority(Red|Yellow|Green|None), description\n" +
            "- add_event : params: title(required), date(YYYY-MM-DD), startTime(HH:MM), endTime(HH:MM), location, color, repeat(none|daily|weekly|biweekly|monthly)\n" +
            "- add_reminder : params: title(required), datetime(YYYY-MM-DDTHH:MM)\n" +
            "- add_habit : params: name(required), icon, duration(days)\n" +
            "- delete_task : params: title(required)\n" +
            "- delete_event : params: title(required)\n" +
            "- reschedule_task : params: title(required), newDate(YYYY-MM-DD)\n" +
            "- reschedule_event : params: title(required), newDate(YYYY-MM-DD), newStartTime(HH:MM)\n" +
            "- complete_task : params: title(required)\n" +
            "- query_schedule : params: date(YYYY-MM-DD)\n" +
            "- show_report : params: {}\n\n" +
            "Rules:\n" +
            "- 'tomorrow' = " + tomorrow + "\n" +
            "- 'tonight' = today " + today + "\n" +
            "- 'lunch' = 12:00, 'noon' = 12:00, 'morning' = 09:00, 'evening' = 18:00\n" +
            "- high priority = Red, medium = Yellow, low = Green\n" +
            "- If user gives a time like '3pm' with a task, use add_event not add_task\n" +
            "- For 'what do I have tomorrow' use action=query_schedule with date=" + tomorrow + "\n" +
            "- For 'reschedule X to Friday' find the next Friday date and use reschedule_task or reschedule_event\n\n" +
            "Example: User says 'Add lunch with Sarah on Thursday at noon'\n" +
            "Response: {\"action\":\"add_event\",\"params\":{\"title\":\"Lunch with Sarah\",\"date\":\"2026-04-02\",\"startTime\":\"12:00\",\"endTime\":\"13:00\"},\"reply\":\"Added lunch with Sarah on Thursday at noon!\"}\n\n" +
            "User's current data:\n" + context;

        String url = "https://openrouter.ai/api/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
            "model", "openrouter/free",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "max_tokens", 300,
            "temperature", 0.1
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://todoapp-a7a8f7f3djb7h8b8.eastus-01.azurewebsites.net");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<?, ?> responseBody = response.getBody();

            if (responseBody == null) {
                return ResponseEntity.status(502).body(Map.of("error", "Empty response from AI service."));
            }

            // Check for API-level error in response body
            if (responseBody.containsKey("error")) {
                Object err = responseBody.get("error");
                return ResponseEntity.status(502).body(Map.of("error", "AI service error: " + err.toString()));
            }

            List<?> choices = (List<?>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                return ResponseEntity.status(502).body(Map.of("error", "AI returned no choices."));
            }

            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            if (message == null) {
                return ResponseEntity.status(502).body(Map.of("error", "AI returned no message."));
            }

            Object contentObj = message.get("content");
            if (contentObj == null) {
                // Some models return finish_reason=content_filter or tool_calls instead
                return ResponseEntity.status(502).body(Map.of("error", "AI returned empty content. Try rephrasing your message."));
            }

            String text = contentObj.toString().strip();

            // Strip markdown code fences if present
            if (text.startsWith("```")) {
                text = text.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```\\s*$", "").strip();
            }

            // Validate it looks like JSON before returning
            if (!text.startsWith("{")) {
                // Try to extract JSON from the response
                int start = text.indexOf('{');
                int end = text.lastIndexOf('}');
                if (start != -1 && end != -1 && end > start) {
                    text = text.substring(start, end + 1);
                } else {
                    // Model returned plain text - wrap it as a 'none' action
                    return ResponseEntity.ok(Map.of("result",
                        "{\"action\":\"none\",\"params\":{},\"reply\":\"" +
                        text.replace("\"", "'") + "\"}"));
                }
            }

            return ResponseEntity.ok(Map.of("result", text));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "AI request failed: " + e.getMessage()));
        }
    }
}
