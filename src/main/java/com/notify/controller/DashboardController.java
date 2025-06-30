package com.notify.controller;

import com.notify.dto.Weather;
import com.notify.model.DiaryEntry;
import com.notify.model.Mood;
import com.notify.model.User;
import com.notify.repository.UserRepository;
import com.notify.security.CustomUserDetails;
import com.notify.service.DiaryService;
import com.notify.service.WeatherService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(value = "lat", required = false) Double lat,
                                @RequestParam(value = "lon", required = false) Double lon,
                                @RequestParam(value = "mood", required = false) String moodFilter,
                                Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = getUser(userDetails);

        // Diary Entries
        List<DiaryEntry> entries = (moodFilter != null && !moodFilter.equalsIgnoreCase("all"))
                ? diaryService.getRecentEntriesByMood(user, moodFilter)
                : diaryService.getRecentEntries(user);

        model.addAttribute("user", user);
        model.addAttribute("entries", entries);
        model.addAttribute("selectedMood", moodFilter != null ? moodFilter : "all");

        // Stats
        model.addAttribute("totalEntries", diaryService.getTotalEntries(user));
        model.addAttribute("entriesThisMonth", diaryService.getEntriesThisMonth(user));
        model.addAttribute("lastEntryDate", diaryService.getLastEntryDate(user));
        model.addAttribute("moodStats", diaryService.getMoodStatistics(user));
        model.addAttribute("monthlyTrends", diaryService.getMonthlyMoodTrends(user));

        // Weather
        Weather weather = (lat != null && lon != null)
                ? weatherService.getWeatherByCoordinates(lat, lon)
                : weatherService.getCurrentWeather();
        model.addAttribute("weather", weather);

        model.addAttribute("newEntry", new DiaryEntry());
        return "dashboard";
    }

    @GetMapping("/diary/search")
    public String searchEntries(@RequestParam("date") String dateString,
                                @RequestParam(value = "lat", required = false) Double lat,
                                @RequestParam(value = "lon", required = false) Double lon,
                                @RequestParam(value = "mood", required = false) String moodFilter,
                                Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = getUser(userDetails);
        model.addAttribute("user", user);

        try {
            LocalDate searchDate = LocalDate.parse(dateString);
            List<DiaryEntry> entries = (moodFilter != null && !moodFilter.equalsIgnoreCase("all"))
                    ? diaryService.getEntriesByDateAndMood(user, searchDate, moodFilter)
                    : diaryService.getEntriesByDate(user, searchDate);

            model.addAttribute("entries", entries);
            model.addAttribute("searchDate", dateString);
            model.addAttribute("selectedMood", moodFilter != null ? moodFilter : "all");
        } catch (Exception e) {
            model.addAttribute("entries", List.of());
            model.addAttribute("error", "Invalid date format");
            model.addAttribute("selectedMood", "all");
        }

        // Stats and Weather
        model.addAttribute("totalEntries", diaryService.getTotalEntries(user));
        model.addAttribute("entriesThisMonth", diaryService.getEntriesThisMonth(user));
        model.addAttribute("lastEntryDate", diaryService.getLastEntryDate(user));
        model.addAttribute("moodStats", diaryService.getMoodStatistics(user));
        model.addAttribute("monthlyTrends", diaryService.getMonthlyMoodTrends(user));

        Weather weather = (lat != null && lon != null)
                ? weatherService.getWeatherByCoordinates(lat, lon)
                : weatherService.getCurrentWeather();
        model.addAttribute("weather", weather);

        model.addAttribute("newEntry", new DiaryEntry());
        return "dashboard";
    }

    @GetMapping("/diary/filter")
    public String filterByMood(@RequestParam("mood") String mood,
                               @RequestParam(value = "lat", required = false) Double lat,
                               @RequestParam(value = "lon", required = false) Double lon,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        return showDashboard(lat, lon, mood, model, userDetails);
    }

    @GetMapping("/api/mood-analytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMoodAnalytics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);

        Map<String, Object> analytics = new HashMap<>();
        Map<String, Long> moodStats = diaryService.getMoodStatistics(user);

        analytics.put("moodDistribution", moodStats);
        analytics.put("monthlyTrends", diaryService.getMonthlyMoodTrends(user));
        analytics.put("recentPattern", diaryService.getRecentMoodPattern(user, 7));

        String dominantMood = moodStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("NEUTRAL");

        analytics.put("dominantMood", dominantMood);

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/api/export-data")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getExportData(@RequestParam(value = "mood", required = false) String moodFilter,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = getUser(userDetails);

        List<DiaryEntry> entries = (moodFilter != null && !moodFilter.equalsIgnoreCase("all"))
                ? diaryService.getAllEntriesByMood(user, moodFilter)
                : diaryService.getAllEntries(user);

        List<Map<String, Object>> exportData = entries.stream()
                .map(entry -> {
                    Map<String, Object> entryMap = new HashMap<>();
                    entryMap.put("title", entry.getTitle());
                    entryMap.put("content", entry.getContent());
                    entryMap.put("mood", entry.getMood() != null ? entry.getMood().toString() : "");
                    entryMap.put("date", entry.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    entryMap.put("moodEmoji", getMoodEmoji(entry.getMood()));
                    return entryMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(exportData);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return "redirect:/login?logout=true";
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private String getMoodEmoji(Mood mood) {
        if (mood == null) return "😐";
        return switch (mood) {
            case HAPPY -> "😊";
            case SAD -> "😢";
            case EXCITED -> "🤩";
            case CALM -> "😌";
            case ANXIOUS -> "😰";
            case GRATEFUL -> "🙏";
            default -> "😐";
        };
    }
}
