package com.todo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class AIProcessor {

    public String processTaskWithAI(String userInput) {
        try {
            // Finding the path to the resources folder dynamically
            String resourcePath = Paths.get("src", "main", "resources", "bridge.py").toAbsolutePath().toString();
            
            // Execute the python script
            ProcessBuilder pb = new ProcessBuilder("python3", resourcePath, userInput);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String result = in.readLine();
            
            return result; 
        } catch (Exception e) {
            return "{\"error\": \"AI process failed: " + e.getMessage() + "\"}";
        }
    }
}
