package com.notify.controller;

import com.notify.model.User;
import com.notify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ Check if user exists and password matches
    @GetMapping("/check-user/{email}")
    public ResponseEntity<String> checkUser(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("❌ User not found with email: " + email);
        }

        User user = userOpt.get();
        return ResponseEntity.ok("✅ User found: " + user.getEmail() +
                ", Password hash: " + user.getPassword());
    }

    // ✅ Test password encoding/matching
    @GetMapping("/test-password/{rawPassword}/{email}")
    public ResponseEntity<String> testPassword(@PathVariable String rawPassword,
                                               @PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("❌ User not found");
        }

        User user = userOpt.get();
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());

        return ResponseEntity.ok("Raw password: " + rawPassword +
                "\nStored hash: " + user.getPassword() +
                "\nMatches: " + matches);
    }

    // ✅ Test password encoding
    @GetMapping("/encode-password/{rawPassword}")
    public ResponseEntity<String> encodePassword(@PathVariable String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        return ResponseEntity.ok("Raw: " + rawPassword + "\nEncoded: " + encoded);
    }
}
