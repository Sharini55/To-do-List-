import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task implements Comparable<Task> {
    
    private String title;
    private String description;
    private boolean isCompleted;
    private LocalDate dueDate;
    private LocalDateTime reminder; // NEW: Stores Date AND Time
    private String priority = "None"; // Can be "Red", "Yellow", "Green", or "None"

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }      

    public Task(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = false; 
        this.reminder = null; // Reminders are empty by default
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getReminder() { return reminder; }
    public void setReminder(LocalDateTime reminder) { this.reminder = reminder; }

    @Override
    public String toString() {
        String checkbox = isCompleted ? "[X]" : "[ ]";
        String remText = (reminder != null) ? " ⏰ Reminder: " + reminder : "";
        return checkbox + " " + title + " (Due: " + dueDate + ")" + remText;
    }

    public String toFileFormat() {
    return title + "|" + description + "|" + dueDate + "|" + isCompleted + "|" + reminder + "|" + priority;
}

    @Override
    public int compareTo(Task other) {
        return this.dueDate.compareTo(other.getDueDate());
    }
}