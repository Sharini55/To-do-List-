package com.todo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "AI API key not configured. Add GEMINI_API_KEY to your Azure app settings."));
        }

        String userMessage = (String) body.get("message");
        String context = (String) body.getOrDefault("context", "");

        String today = java.time.LocalDate.now().toString();
        String tomorrow = java.time.LocalDate.now().plusDays(1).toString();

        String systemPrompt = "You are a productivity assistant. Today=" + today + ", Tomorrow=" + tomorrow + ".\n"
            + "Reply ONLY with valid JSON (no markdown, no backticks):\n"
            + "{\"action\":\"<action>\",\"params\":{...},\"reply\":\"<message>\"}\n"
            + "Actions: add_task(title,dueDate,priority[Red/Yellow/Green/None],description), "
            + "add_event(title,date,startTime,endTime,location,color,repeat[none/daily/weekly/biweekly/monthly]), "
            + "add_reminder(title,datetime), add_habit(name,icon,duration), "
            + "delete_task(title), delete_event(title), "
            + "reschedule_task(title,newDate), reschedule_event(title,newDate,newStartTime), "
            + "complete_task(title), query_schedule(date), show_report, none.\n"
            + "Priority: high=Red, medium=Yellow, low=Green, default=None.\n"
            + "Context: " + context;

        String url = "https://openrouter.ai/api/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
            "model", "mistralai/mistral-7b-instruct:free",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "max_tokens", 256,
            "temperature", 0.1
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://todoapp-a7a8f7f3djb7h8b8.eastus-01.azurewebsites.net");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            var choices = (List<?>) response.getBody().get("choices");
            var message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String text = (String) message.get("content");

            text = text.strip();
            if (text.startsWith("```")) {
                text = text.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
            }

            return ResponseEntity.ok(Map.of("result", text));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "AI request failed: " + e.getMessage()));
        }
    }
}
