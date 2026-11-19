package com.snippetmanager.controller;

import com.snippetmanager.model.User;
import com.snippetmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listRegisteredUsers() {
        List<Map<String, Object>> safeUsers = userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "username", u.getUsername(),
                        "role", u.getRole()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(safeUsers);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String identifier = body.getOrDefault("identifier", "").trim();
        String password = body.getOrDefault("password", "");

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByNameIgnoreCase(identifier);
        }

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid name/username or password"));
        }

        User u = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "username", u.getUsername(),
                "role", u.getRole()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        String username = body.getOrDefault("username", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");

        if (name.isBlank() || username.isBlank() || password.length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name, username, and password (min 4 chars) are required"));
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()
                || userRepository.findByNameIgnoreCase(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "A member with that name or username already exists"));
        }

        User saved = userRepository.save(new User(name, username, password, "Member"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "username", saved.getUsername(),
                "role", saved.getRole()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String currentPassword = body.getOrDefault("currentPassword", "");
        String newPassword = body.getOrDefault("newPassword", "");

        if (newPassword.length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "New password must be at least 4 characters"));
        }

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(currentPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Current password is incorrect"));
        }

        User u = userOpt.get();
        u.setPassword(newPassword);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
