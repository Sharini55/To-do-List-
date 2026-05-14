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
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }

    // Model priority list — tries each until one works
    private static final String[] MODELS = {
        "google/gemini-2.0-flash-exp:free",
        "meta-llama/llama-3.3-70b-instruct:free",
        "mistralai/mistral-7b-instruct:free"
    };

    @PostMapping("/chat")
public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
    String userMessage = (String) body.get("message");
    if (userMessage == null || userMessage.isBlank()) {
        return ResponseEntity.badRequest().body(Map.of("error", "No message provided."));
    }

    // NEW: Use your custom AIProcessor instead of the external API
    AIProcessor localBrain = new AIProcessor();
    String aiResult = localBrain.processTaskWithAI(userMessage);

    // This returns the JSON your bridge.py created: {"intent": "create", "task": "..."}
    return ResponseEntity.ok(Map.of("result", aiResult));
}

        String prompt =
            "You are a productivity assistant that EXECUTES actions for the user.\n" +
            "Today: " + today + " (" + dayOfWeek + "). Tomorrow: " + tomorrow + ".\n" +
            "Next days: " + nextDays + "\n\n" +
            "CRITICAL: Respond with ONLY a valid JSON object. No markdown, no explanation, nothing else.\n\n" +
            "Format: {\"action\":\"ACTION\",\"params\":{...},\"reply\":\"short confirmation\"}\n\n" +
            "ACTIONS:\n" +
            "add_task      params: title*, dueDate(YYYY-MM-DD), priority(Red|Yellow|Green|None), description\n" +
            "add_event     params: title*, date(YYYY-MM-DD), startTime(HH:MM), endTime(HH:MM), location, color(hex), repeat(none|daily|weekly|biweekly|monthly)\n" +
            "add_reminder  params: title*, datetime(YYYY-MM-DDTHH:MM)\n" +
            "add_habit     params: name*, icon(emoji), duration(number)\n" +
            "delete_task   params: title* (partial match ok)\n" +
            "delete_event  params: title*\n" +
            "complete_task params: title*\n" +
            "reschedule_task  params: title*, newDate(YYYY-MM-DD)\n" +
            "reschedule_event params: title*, newDate(YYYY-MM-DD), newStartTime(HH:MM)\n" +
            "query_schedule   params: date(YYYY-MM-DD)\n" +
            "show_report      params: {}\n" +
            "none             params: {} — only for pure questions, answer in reply\n\n" +
            "RULES:\n" +
            "- 'today' = " + today + ", 'tomorrow' = " + tomorrow + "\n" +
            "- 'tonight/this evening' = today with time ~18:00-20:00\n" +
            "- 'high/urgent/important' priority = Red, 'medium/normal' = Yellow, 'low' = Green\n" +
            "- If user says 'add task X today' -> action=add_task, dueDate=" + today + "\n" +
            "- If user says 'add task X tomorrow' -> action=add_task, dueDate=" + tomorrow + "\n" +
            "- If user mentions a specific time -> prefer add_event over add_task\n" +
            "- For 'what do I have tomorrow' -> action=query_schedule, date=" + tomorrow + "\n" +
            "- For 'plan my day' -> action=show_report (frontend handles it)\n" +
            "- reply must be short (1 sentence max)\n\n" +
            "EXAMPLE - User: 'add a task today to work on my cs hw with high priority'\n" +
            "RESPONSE: {\"action\":\"add_task\",\"params\":{\"title\":\"Work on CS homework\",\"dueDate\":\"" + today + "\",\"priority\":\"Red\"},\"reply\":\"Added high priority task: Work on CS homework for today!\"}\n\n" +
            "EXAMPLE - User: 'add physics hw due friday'\n" +
            "RESPONSE: {\"action\":\"add_task\",\"params\":{\"title\":\"Physics homework\",\"dueDate\":\"" + nextDays.getOrDefault("friday", today) + "\",\"priority\":\"None\"},\"reply\":\"Added Physics homework due Friday!\"}\n\n" +
            "User's current app data:\n" + context + "\n\n" +
            "User message: " + userMessage;

        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://todoapp-a7a8f7f3djb7h8b8.eastus-01.azurewebsites.net");
        headers.set("X-Title", "Smart Todo App");

        // Try models in order until one succeeds
        Exception lastException = null;
        for (String model : MODELS) {
            try {
                Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 300,
                    "temperature", 0.1,
                    "response_format", Map.of("type", "json_object")
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
                Map<?, ?> responseBody = response.getBody();

                if (responseBody == null || responseBody.containsKey("error")) continue;

                List<?> choices = (List<?>) responseBody.get("choices");
                if (choices == null || choices.isEmpty()) continue;

                Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) choice.get("message");
                if (message == null) continue;

                Object contentObj = message.get("content");
                if (contentObj == null || contentObj.toString().isBlank()) continue;

                String text = contentObj.toString().strip();

                // Strip markdown fences
                if (text.startsWith("```")) {
                    text = text.replaceAll("^```[a-zA-Z]*\\n?", "")
                               .replaceAll("```\\s*$", "").strip();
                }

                // Extract JSON if surrounded by other text
                if (!text.startsWith("{")) {
                    int start = text.indexOf('{');
                    int end = text.lastIndexOf('}');
                    if (start != -1 && end > start) {
                        text = text.substring(start, end + 1);
                    } else {
                        // Wrap plain text reply as none action
                        return ResponseEntity.ok(Map.of("result",
                            "{\"action\":\"none\",\"params\":{},\"reply\":\"" +
                            text.replace("\"", "'").replace("\n", " ") + "\"}"));
                    }
                }

                return ResponseEntity.ok(Map.of("result", text));

            } catch (Exception e) {
                lastException = e;
                // Try next model
            }
        }

        String errMsg = lastException != null ? lastException.getMessage() : "All models failed";
        return ResponseEntity.status(500).body(Map.of("error", "AI unavailable: " + errMsg));
    }
}
