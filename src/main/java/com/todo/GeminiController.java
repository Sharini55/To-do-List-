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
                .body(Map.of("error", "Gemini API key not configured."));
        }

        String userMessage = (String) body.get("message");
        String context = (String) body.getOrDefault("context", "");

        // Keep the prompt short to stay within free tier token limits
        String today = java.time.LocalDate.now().toString();
        String tomorrow = java.time.LocalDate.now().plusDays(1).toString();

        String prompt = "You are a productivity assistant. Today=" + today + ", Tomorrow=" + tomorrow + ".\n"
            + "Reply ONLY with valid JSON (no markdown, no backticks):\n"
            + "{\"action\":\"<action>\",\"params\":{...},\"reply\":\"<message>\"}\n"
            + "Actions: add_task(title,dueDate,priority[Red/Yellow/Green/None],description), "
            + "add_event(title,date,startTime,endTime,location,color,repeat[none/daily/weekly/biweekly/monthly]), "
            + "add_reminder(title,datetime), add_habit(name,icon,duration), "
            + "delete_task(title), delete_event(title), "
            + "reschedule_task(title,newDate), reschedule_event(title,newDate,newStartTime), "
            + "complete_task(title), query_schedule(date), show_report, none.\n"
            + "Priority: high=Red, medium=Yellow, low=Green, default=None.\n"
            + "Context: " + context + "\n"
            + "User: " + userMessage;

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + geminiApiKey;

        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", prompt)
                })
            },
            "generationConfig", Map.of(
                "temperature", 0.1,
                "maxOutputTokens", 256
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
