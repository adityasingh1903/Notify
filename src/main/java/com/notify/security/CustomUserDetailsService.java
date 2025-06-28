package com.notify.security;

import com.notify.model.User;
import com.notify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Attempting to load user with email: " + email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found with email: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Remove the redundant null check - it's unreachable code

        System.out.println("✅ User found: " + user.getEmail());
        System.out.println("📋 User ID: " + user.getId());
        System.out.println("🔐 Password hash (first 20 chars): " +
                (user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "NULL"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        System.out.println("🎯 CustomUserDetails created successfully");

        return userDetails;
    }
}