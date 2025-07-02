package com.notify.controller;

import com.notify.model.DiaryEntry;
import com.notify.model.User;
import com.notify.repository.UserRepository;
import com.notify.security.CustomUserDetails;
import com.notify.service.DiaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public String createEntry(@Valid @ModelAttribute("newEntry") DiaryEntry entry,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fill all required fields");
            return "redirect:/dashboard";
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        entry.setUser(user);

        try {
            diaryService.saveEntry(entry);
            redirectAttributes.addFlashAttribute("success", "Entry created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create entry");
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/view/{id}")
    public String viewEntry(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<DiaryEntry> entry = diaryService.getEntryById(id, user);

        if (entry.isPresent()) {
            model.addAttribute("entry", entry.get());
            return "diary/view";
        } else {
            return "redirect:/dashboard?error=Entry not found";
        }
    }

    @GetMapping("/edit/{id}")
    public String editEntry(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<DiaryEntry> entry = diaryService.getEntryById(id, user);

        if (entry.isPresent()) {
            DiaryEntry diaryEntry = entry.get();

            java.util.Date createdDate = java.util.Date.from(
                    diaryEntry.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toInstant()
            );

            model.addAttribute("entry", diaryEntry);
            model.addAttribute("createdDate", createdDate);
            return "diary/edit";
        } else {
            return "redirect:/dashboard?error=Entry not found";
        }
    }

    @PostMapping("/update/{id}")
    public String updateEntry(@PathVariable Long id,
                              @Valid @ModelAttribute("entry") DiaryEntry updatedEntry,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("entry", updatedEntry);
            return "diary/edit";
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<DiaryEntry> existingEntry = diaryService.getEntryById(id, user);

        if (existingEntry.isPresent()) {
            DiaryEntry entry = existingEntry.get();

            if (updatedEntry.getEntryDate() == null) {
                redirectAttributes.addFlashAttribute("error", "Entry date cannot be empty");
                return "redirect:/diary/edit/" + id;
            }

            entry.setTitle(updatedEntry.getTitle());
            entry.setContent(updatedEntry.getContent());
            entry.setMood(updatedEntry.getMood());
            entry.setEntryDate(updatedEntry.getEntryDate());

            diaryService.saveEntry(entry);
            redirectAttributes.addFlashAttribute("success", "Entry updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Entry not found");
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            diaryService.deleteEntry(id, user);
            redirectAttributes.addFlashAttribute("success", "Entry deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete entry");
        }

        return "redirect:/dashboard";
    }
}
