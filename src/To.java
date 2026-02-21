import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class To {
    public static void main(String[] args) {

        ArrayList<Task> taskList = loadTasks();
        System.out.println("Loaded " + taskList.size() + " tasks from file."); 
        
        System.out.println("\n--- ALERTS ---");
        boolean hasAlerts = false;
        for (Task t : taskList) {
            if (!t.isCompleted() && (t.getDueDate().isBefore(LocalDate.now()) || t.getDueDate().isEqual(LocalDate.now()))) {
                System.out.println(t); 
                hasAlerts = true;
            }
        }
        if (!hasAlerts) System.out.println("You are all caught up! No tasks overdue.");

        Scanner scan = new Scanner(System.in);
        int choice;
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n--- TO DO LIST ---");
            System.out.println("1. Add Task");
            System.out.println("2. Delete Task");
            System.out.println("3. Edit Task");
            System.out.println("4. View Tasks");
            System.out.println("5. Sort Tasks by Date"); 
            System.out.println("6. Exit");
            System.out.print("Select an option: ");

            choice = getValidInt(scan);

            switch (choice) {
                case 1:
                    System.out.print("Enter Task Title: ");
                    String title = scan.nextLine();

                    System.out.print("Enter Description: ");
                    String desc = scan.nextLine();

                    System.out.print("Enter Due Date (YYYY-MM-DD): ");
                    LocalDate date = getValidDate(scan); 

                    Task newTask = new Task(title, desc, date);
                    taskList.add(newTask);
                    System.out.println("Task added successfully!");
                    break;

                case 2:
                    System.out.println("--- DELETE TASK ---");
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks to delete!");
                    } else {
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + ". " + taskList.get(i));
                        }

                        System.out.print("Enter the number of the task to delete: ");
                        int deleteId = getValidInt(scan); 
                        int index = deleteId - 1;

                        if (index >= 0 && index < taskList.size()) {
                            Task removedTask = taskList.remove(index); 
                            System.out.println("Removed: " + removedTask.getTitle());
                        } else {
                            System.out.println("Invalid number. Task does not exist.");
                        }
                    }
                    break;

                case 3:
                    System.out.println("--- EDIT TASK ---");
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks to edit!");
                    } else {
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + ". " + taskList.get(i));
                        }

                        System.out.print("Enter the number of the task to edit: ");
                        int editId = getValidInt(scan);
                        int index = editId - 1;

                        if (index >= 0 && index < taskList.size()) {
                            Task taskToEdit = taskList.get(index);

                            System.out.println("Editing: " + taskToEdit.getTitle());
                            System.out.println("1. Mark as Completed/Incomplete");
                            System.out.println("2. Change Title");
                            System.out.println("3. Change Date");
                            System.out.print("Select option: ");

                            int editOption = getValidInt(scan);

                            switch (editOption) {
                                case 1:
                                    taskToEdit.setCompleted(!taskToEdit.isCompleted());
                                    System.out.println("Status updated!");
                                    break;
                                case 2:
                                    System.out.print("Enter new title: ");
                                    String newTitle = scan.nextLine();
                                    taskToEdit.setTitle(newTitle); 
                                    System.out.println("Title updated!");
                                    break;
                                case 3:
                                    System.out.print("Enter new date (YYYY-MM-DD): ");
                                    LocalDate newDate = getValidDate(scan);
                                    taskToEdit.setDueDate(newDate);
                                    System.out.println("Date updated!");
                                    break;
                                default:
                                    System.out.println("Invalid option.");
                            }
                        } else {
                            System.out.println("Invalid task number.");
                        }
                    }
                    break;

                case 4:
                    System.out.println("\nYour Tasks:");
                    if (taskList.isEmpty()) {
                        System.out.println("No tasks yet!");
                    } else {
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + ". " + taskList.get(i));
                        }
                    }
                    break;

                case 5:
                    System.out.println("Sorting tasks by Due Date...");
                    Collections.sort(taskList);
                    System.out.println("Tasks sorted! Press 4 to view them.");
                    break;

                case 6:
                    System.out.println("Saving and Exiting...");
                    saveTasks(taskList); 
                    isRunning = false;
                    break;

                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
        scan.close();
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

    public static ArrayList<Task> loadTasks() {
        ArrayList<Task> loadedTasks = new ArrayList<>();
        File file = new File("tasks.txt");

        if (!file.exists()) {
            return loadedTasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split("\\|");

                    if (parts.length >= 4) { // Needs at least 4 parts
                        String title = parts[0];
                        String desc = parts[1];
                        LocalDate date = LocalDate.parse(parts[2]); 
                        boolean isDone = Boolean.parseBoolean(parts[3]); 

                        Task t = new Task(title, desc, date);
                        t.setCompleted(isDone); 
                        
                        // If the file has a 5th part (the reminder), load it!
                        if (parts.length == 5 && !parts[4].equals("null")) {
                            t.setReminder(LocalDateTime.parse(parts[4]));
                        }
                        
                        loadedTasks.add(t);
                    }
                } catch (Exception e) {
                    System.out.println("Skipping corrupted task data.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
        return loadedTasks;
    }

    public static int getValidInt(Scanner scan) {
        while (true) {
            if (scan.hasNextInt()) {
                int num = scan.nextInt();
                scan.nextLine(); 
                return num;
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scan.nextLine(); 
                System.out.print("Try again: ");
            }
        }
    }

    public static LocalDate getValidDate(Scanner scan) {
        while (true) {
            String input = scan.nextLine();
            try {
                LocalDate enteredDate = LocalDate.parse(input);
                if (enteredDate.isBefore(LocalDate.now())) {
                    System.out.print("You can't schedule a task in the past! Enter a future date: ");
                    continue; 
                }
                return enteredDate;
            } catch (DateTimeParseException e) {
                System.out.print("Invalid format! Please use YYYY-MM-DD (e.g., 2026-10-31): ");
            }
        }
    }
}