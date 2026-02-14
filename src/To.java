import java.time.LocalDate;
import java.util.ArrayList; // 1. Import ArrayList
import java.util.Scanner;
import java.io.*;

public class To {
    public static void main(String[] args) {

        ArrayList<Task> taskList = loadTasks();
        System.out.println("Loaded " + taskList.size() + " tasks from file."); // Confirmation message

        Scanner scan = new Scanner(System.in);
        int choice;
        boolean isRunning = true;

        // 3. The "Game Loop" - keeps the program running
        while (isRunning) {
            System.out.println("\\n--- TO DO LIST ---");
            System.out.println("1. Add Task");
            System.out.println("2. Delete Task");
            System.out.println("3. Edit Task");
            System.out.println("4. View Tasks");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");


            if (scan.hasNextInt()) {
                choice = scan.nextInt();
                scan.nextLine(); //
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scan.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    // --- ADD TASK LOGIC ---
                    System.out.print("Enter Task Title: ");
                    String title = scan.nextLine();

                    System.out.print("Enter Description: ");
                    String desc = scan.nextLine();

                    // Simple Date Handling
                    System.out.print("Enter Due Date (YYYY-MM-DD): ");
                    String dateInput = scan.nextLine();
                    LocalDate date = LocalDate.parse(dateInput); // Converts String to Date

                    // Create object and ADD to list
                    Task newTask = new Task(title, desc, date);
                    taskList.add(newTask);
                    System.out.println("Task added successfully!");
                    break;

                case 2:
                    System.out.println("--- DELETE TASK ---");
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks to delete!");
                    } else {
                        // 1. Show the list
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + ". " + taskList.get(i));
                        }

                        // 2. Ask for ID
                        System.out.print("Enter the number of the task to delete: ");
                        if (scan.hasNextInt()) {
                            int deleteId = scan.nextInt();
                            scan.nextLine(); // Eat the newline!

                            // 3. Calculate Index
                            int index = deleteId - 1;

                            // 4. Validate Index (Prevent Crash)
                            if (index >= 0 && index < taskList.size()) {
                                Task removedTask = taskList.remove(index); // Removes and returns the task
                                System.out.println("Removed: " + removedTask.getTitle());
                            } else {
                                System.out.println("Invalid number. Task does not exist.");
                            }
                        } else {
                            System.out.println("Invalid input.");
                            scan.nextLine(); // Clear garbage
                        }
                    }
                    break;

                case 3:
                    System.out.println("--- EDIT TASK ---");
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks to edit!");
                    } else {
                        // 1. Show list (Copy-paste logic or make a helper method)
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + ". " + taskList.get(i));
                        }

                        System.out.print("Enter the number of the task to edit: ");
                        if (scan.hasNextInt()) {
                            int editId = scan.nextInt();
                            scan.nextLine();
                            int index = editId - 1;

                            if (index >= 0 && index < taskList.size()) {
                                // 2. GET the specific task object
                                Task taskToEdit = taskList.get(index);

                                // 3. Ask what to change
                                System.out.println("Editing: " + taskToEdit.getTitle());
                                System.out.println("1. Mark as Completed/Incomplete");
                                System.out.println("2. Change Title");
                                System.out.println("3. Change Date");
                                System.out.print("Select option: ");

                                int editOption = scan.nextInt();
                                scan.nextLine(); // Eat newline

                                switch (editOption) {
                                    case 1:
                                        // Toggle logic: If true becomes false, if false becomes true
                                        boolean currentStatus = taskToEdit.isCompleted();
                                        taskToEdit.setCompleted(!currentStatus);
                                        System.out.println("Status updated!");
                                        break;
                                    case 2:
                                        System.out.print("Enter new title: ");
                                        String newTitle = scan.nextLine();
                                        taskToEdit.setTitle(newTitle); // Updates the object inside the list
                                        System.out.println("Title updated!");
                                        break;
                                    case 3:
                                        System.out.print("Enter new date (YYYY-MM-DD): ");
                                        String newDateStr = scan.nextLine();
                                        taskToEdit.setDueDate(LocalDate.parse(newDateStr));
                                        System.out.println("Date updated!");
                                        break;
                                    default:
                                        System.out.println("Invalid option.");
                                }

                            } else {
                                System.out.println("Invalid task number.");
                            }
                        }
                    }
                    break;

                case 4:
                    // --- VIEW TASK LOGIC ---
                    System.out.println("\\nYour Tasks:");
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks yet!");
                    } else {
                        // Loop through the list and print each one
                        for (int i = 0; i < taskList.size(); i++) {
                            // (i+1) makes it look like 1. instead of 0.
                            System.out.println((i + 1) + ". " + taskList.get(i));
                        }
                    }
                    break;

                case 5:
                    System.out.println("Saving and Exiting...");
                    saveTasks(taskList); // <--- 2. SAVE BEFORE EXITING
                    isRunning = false;
                    break;

                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
        scan.close();
    }
    // 1. SAVE METHOD: Writes the list to "tasks.txt"
    public static void saveTasks(ArrayList<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (Task t : tasks) {
                writer.write(t.toFileFormat());
                writer.newLine(); // Move to next line
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // 2. LOAD METHOD: Reads "tasks.txt" and rebuilds the ArrayList
    public static ArrayList<Task> loadTasks() {
        ArrayList<Task> loadedTasks = new ArrayList<>();
        File file = new File("tasks.txt");

        // If file doesn't exist (first time running), just return empty list
        if (!file.exists()) {
            return loadedTasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line by the pipe symbol "|"
                // Note: split("\\|") is needed because | is a special regex character
                String[] parts = line.split("\\|");

                if (parts.length == 4) {
                    String title = parts[0];
                    String desc = parts[1];
                    LocalDate date = LocalDate.parse(parts[2]); // Convert String back to Date
                    boolean isDone = Boolean.parseBoolean(parts[3]); // Convert String back to Boolean

                    Task t = new Task(title, desc, date);
                    t.setCompleted(isDone); // Restore the completion status
                    loadedTasks.add(t);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
        return loadedTasks;
    }


}