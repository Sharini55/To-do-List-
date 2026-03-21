package com.todo;

import java.time.LocalDate;

public class Task {
    private static int counter = 0;

    private int id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDate dueDate;
    private String priority; // "Red", "Yellow", "Green", "None"

    public Task() {}

    public Task(String title, String description, LocalDate dueDate, String priority) {
        this.id = ++counter;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority == null ? "None" : priority;
        this.completed = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
