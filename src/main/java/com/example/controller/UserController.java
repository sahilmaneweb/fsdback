package com.example.controller;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Update password
    @PutMapping
    public Map<String, String> updatePassword(@RequestParam String username, 
                                              @RequestParam String oldPassword, 
                                              @RequestParam String newPassword) {
        User user = userRepository.findByUsername(username);
        Map<String, String> response = new HashMap<>();
        
        if (user != null && user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            userRepository.save(user);
            response.put("message", "Password updated successfully");
        } else {
            response.put("error", "Old password is incorrect");
        }
        
        return response;
    }

    // Login - Returns JWT and role (JWT logic is skipped for now)
    @PostMapping
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        User user = userRepository.findByUsername(username);
        Map<String, String> response = new HashMap<>();

        if (user != null && user.getPassword().equals(password)) {
            response.put("token", "jwt-token-placeholder");  // Placeholder for actual JWT logic
            response.put("role", user.getRole());
        } else {
            response.put("error", "Invalid username or password");
        }

        return response;
    }
}
