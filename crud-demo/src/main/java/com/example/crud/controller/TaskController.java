package com.example.crud.controller;

import com.example.crud.model.Task;
import com.example.crud.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// "*" allows any origin - fine for local development.
// For production, replace with your actual frontend URL.
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task saved = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // READ all
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // READ one
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task updated) {
        return taskRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setDescription(updated.getDescription());
                    existing.setDueDate(updated.getDueDate());
                    return ResponseEntity.ok(taskRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
