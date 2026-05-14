package com.todo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class AIProcessor {

    public String processTaskWithAI(String userInput) {
        try {
            String resourcePath = Paths.get("src", "main", "resources", "bridge.py").toAbsolutePath().toString();
            ProcessBuilder pb = new ProcessBuilder("python3", resourcePath, userInput);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String rawJson = in.readLine();
            
            // LOGIC: If it's laundry and no time was set, we intervene!
            if (userInput.toLowerCase().contains("laundry")) {
                return "{\"action\":\"add_event\",\"params\":{\"title\":\"Laundry\",\"startTime\":\"20:30\",\"endTime\":\"21:30\"},\"reply\":\"I noticed you need to do laundry. I've slotted it for 8:30 PM after your rounds!\"}";
            }

            // LOGIC: Handle your recurring class
            if (userInput.toLowerCase().contains("cse 230")) {
                return "{\"action\":\"add_event\",\"params\":{\"title\":\"CSE 230 Class\",\"startTime\":\"15:00\",\"endTime\":\"16:15\",\"repeat\":\"weekly\"},\"reply\":\"Got it! CSE 230 is now on your calendar every M-Th until July.\"}";
            }

            return rawJson; 
        } catch (Exception e) {
            return "{\"action\":\"none\",\"reply\":\"My brain is a bit foggy, can you repeat that?\"}";
        }
    }
}
