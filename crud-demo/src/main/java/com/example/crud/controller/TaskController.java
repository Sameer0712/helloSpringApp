package com.example.crud.controller;

import com.example.crud.model.Task;
import com.example.crud.model.User;
import com.example.crud.repository.TaskRepository;
import com.example.crud.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Resolves the logged-in user from the JWT that Spring Security has
    // already validated by the time a controller method runs.
    private User currentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        task.setUser(currentUser(authentication));
        Task saved = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // READ all (only this user's tasks)
    @GetMapping
    public List<Task> getAllTasks(Authentication authentication) {
        return taskRepository.findByUserUsername(authentication.getName());
    }

    // READ one (only if it belongs to this user)
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, Authentication authentication) {
        return taskRepository.findByIdAndUserUsername(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE (only if it belongs to this user)
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task updated,
            Authentication authentication
    ) {
        return taskRepository.findByIdAndUserUsername(id, authentication.getName())
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setDescription(updated.getDescription());
                    existing.setDueDate(updated.getDueDate());
                    return ResponseEntity.ok(taskRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // TOGGLE complete/pending (only if it belongs to this user)
    @PatchMapping("/{id}/toggle-complete")
    public ResponseEntity<Task> toggleComplete(@PathVariable Long id, Authentication authentication) {
        return taskRepository.findByIdAndUserUsername(id, authentication.getName())
                .map(task -> {
                    task.setCompleted(!task.isCompleted());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (only if it belongs to this user)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        return taskRepository.findByIdAndUserUsername(id, authentication.getName())
                .map(task -> {
                    taskRepository.delete(task);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}