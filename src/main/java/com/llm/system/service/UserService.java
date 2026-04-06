package com.llm.system.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.llm.system.entity.User;
import com.llm.system.repository.UserRepository;

// This service handles all User-related logic (Registration, Login helpers)
@Service
public class UserService {

    // Access to the User database table
    @Autowired
    private UserRepository userRepository;

    // Tool to encrypt passwords (make them unreadable for security)
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Method to register a NEW user
    public String registerUser(User user) {
        // Step 1: Check if the email is already in use
        Optional<User> existingEmail = userRepository.findByEmail(user.getEmail());
        if (existingEmail.isPresent()) {
            return "Error: Email already registered!";
        }
        
        // Step 2: Check if the username is already taken
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return "Error: Username tokens already taken!";
        }

        // Step 3: Encrypt the password
        // We never store plain text passwords. "encode" turns "123456" into something like "$2a$10$..."
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // Step 4: Save the new user to the database
        userRepository.save(user);
        
        // Return a success message
        return "Success: User registered successfully!";
    }

    // specific helper method to find a user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
