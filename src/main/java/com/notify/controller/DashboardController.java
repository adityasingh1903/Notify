package com.notify.controller;

import com.notify.model.User;
import com.notify.model.DiaryEntry;
import com.notify.repository.UserRepository;
import com.notify.security.CustomUserDetails;
import com.notify.service.DiaryService;
import com.notify.service.WeatherService;
import com.notify.dto.Weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

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
                                Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Get current user
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("user", user);

        // Get diary entries
        List<DiaryEntry> entries = diaryService.getRecentEntries(user);
        model.addAttribute("entries", entries);

        // Stats
        model.addAttribute("totalEntries", diaryService.getTotalEntries(user));
        model.addAttribute("entriesThisMonth", diaryService.getEntriesThisMonth(user));
        model.addAttribute("lastEntryDate", diaryService.getLastEntryDate(user));

        // Weather: Prefer coordinates if passed
        Weather weather = (lat != null && lon != null)
                ? weatherService.getWeatherByCoordinates(lat, lon)
                : weatherService.getCurrentWeather(); // fallback

        model.addAttribute("weather", weather);

        // Form object for modal
        model.addAttribute("newEntry", new DiaryEntry());

        return "dashboard";
    }

    @GetMapping("/diary/search")
    public String searchEntries(@RequestParam("date") String dateString,
                                @RequestParam(value = "lat", required = false) Double lat,
                                @RequestParam(value = "lon", required = false) Double lon,
                                Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            LocalDate searchDate = LocalDate.parse(dateString);
            List<DiaryEntry> entries = diaryService.getEntriesByDate(user, searchDate);

            model.addAttribute("entries", entries);
            model.addAttribute("searchDate", dateString);
        } catch (Exception e) {
            model.addAttribute("entries", List.of());
            model.addAttribute("error", "Invalid date format");
        }

        // Stats
        model.addAttribute("user", user);
        model.addAttribute("totalEntries", diaryService.getTotalEntries(user));
        model.addAttribute("entriesThisMonth", diaryService.getEntriesThisMonth(user));
        model.addAttribute("lastEntryDate", diaryService.getLastEntryDate(user));

        // Weather
        Weather weather = (lat != null && lon != null)
                ? weatherService.getWeatherByCoordinates(lat, lon)
                : weatherService.getCurrentWeather();

        model.addAttribute("weather", weather);
        model.addAttribute("newEntry", new DiaryEntry());

        return "dashboard";
    }
}
