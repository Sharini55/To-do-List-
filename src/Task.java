import java.time.LocalDate;

// 1. ADDED: "implements Comparable<Task>" so Java knows how to sort it
public class Task implements Comparable<Task> {
    
    private String title;
    private String description;
    private boolean isCompleted;
    private LocalDate dueDate;

    public Task(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = false; 
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // 2. ADDED: Logic to check if overdue or due today
    @Override
    public String toString() {
        String checkbox = isCompleted ? "[X]" : "[ ]";
        String statusAlert = "";

        // Only show alerts if the task is NOT completed
        if (!isCompleted) {
            LocalDate today = LocalDate.now();
            if (dueDate.isBefore(today)) {
                statusAlert = " ⚠️ OVERDUE!";
            } else if (dueDate.isEqual(today)) {
                statusAlert = " ⏰ DUE TODAY!";
            }
        }

        return checkbox + " " + title + " (Due: " + dueDate + ")" + statusAlert;
    }

    public String toFileFormat() {
        return title + "|" + description + "|" + dueDate + "|" + isCompleted;
    }

    // 3. ADDED: This method tells Java how to sort Tasks (by Due Date)
    @Override
    public int compareTo(Task other) {
        return this.dueDate.compareTo(other.getDueDate());
    }
}