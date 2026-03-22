package com.todo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
public class TaskController {

    private List<Task> tasks = new ArrayList<>();

    @GetMapping("/api/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/api/tasks")
    public ResponseEntity<?> addTask(@RequestBody Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Task title cannot be empty"));
        }
        task.setId(tasks.size() + 1);
        if (task.getPriority() == null) task.setPriority("None");
        tasks.add(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PatchMapping("/api/tasks/{id}/complete")
    public ResponseEntity<?> toggleComplete(@PathVariable int id) {
        return tasks.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .map(t -> {
                t.setCompleted(!t.isCompleted());
                return ResponseEntity.ok(t);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable int id, @RequestBody Task updated) {
        return tasks.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .map(t -> {
                if (updated.getTitle() != null) t.setTitle(updated.getTitle());
                if (updated.getDueDate() != null) t.setDueDate(updated.getDueDate());
                if (updated.getPriority() != null) t.setPriority(updated.getPriority());
                if (updated.getDescription() != null) t.setDescription(updated.getDescription());
                return ResponseEntity.ok(t);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "tasks", String.valueOf(tasks.size())));
    }
}
