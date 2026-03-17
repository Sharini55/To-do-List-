import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class To {
    
    public static ArrayList<Task> loadTasks() {
        ArrayList<Task> loadedTasks = new ArrayList<>();
        File file = new File("tasks.txt");

        if (!file.exists()) {
            return loadedTasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) { 
                        // .trim() strips out hidden corrupted spaces and carriage returns!
                        String title = parts[0].trim();
                        String desc = parts[1].trim();
                        LocalDate date = LocalDate.parse(parts[2].trim()); 
                        boolean isDone = Boolean.parseBoolean(parts[3].trim()); 

                        Task t = new Task(title, desc, date);
                        t.setCompleted(isDone); 
                        
                        if (parts.length >= 5 && !parts[4].trim().equals("null")) {
                            t.setReminder(LocalDateTime.parse(parts[4].trim()));
                        }
                        if (parts.length >= 6 && !parts[5].trim().equals("null")) {
                            t.setPriority(parts[5].trim());
                        }
                        loadedTasks.add(t);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR loading a task line: " + line);
                    System.out.println("Reason: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        }
        return loadedTasks;
    }

    public static void saveTasks(ArrayList<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (Task t : tasks) {
                writer.write(t.toFileFormat());
                writer.newLine(); 
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }
}