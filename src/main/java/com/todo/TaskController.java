package com.todo;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private List<Task> tasks = new ArrayList<>();

    @GetMapping
    public List<Task> getAllTasks() {
        return tasks;
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) {
        task.setId(tasks.size() + 1);
        if (task.getPriority() == null) task.setPriority("None");
        tasks.add(task);
        return task;
    }

    @PatchMapping("/{id}/complete")
    public Task toggleComplete(@PathVariable int id) {
        return tasks.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .map(t -> { t.setCompleted(!t.isCompleted()); return t; })
            .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable int id) {
        tasks.removeIf(t -> t.getId() == id);
    }
}
