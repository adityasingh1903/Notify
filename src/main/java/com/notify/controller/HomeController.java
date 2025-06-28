package com.notify.controller;

import com.notify.model.User;
import com.notify.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Home page
    @GetMapping("/")
    public String getHome() {
        return "home";
    }
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/features")
    public String features() {
        return "features";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    // Signup form page
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // Register user
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "signup";
        }

        if (userRepo.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already in use");
            return "signup";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);

        model.addAttribute("success", "Account created successfully!");
        model.addAttribute("user", new User());
        return "signup";
    }

    // Login page
    @GetMapping("/login")
    public String getLogin(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    // Logout handler (optional, Spring Security also manages logout)
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
