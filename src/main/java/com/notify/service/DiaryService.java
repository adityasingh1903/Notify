package com.notify.service;

import com.notify.model.DiaryEntry;
import com.notify.model.User;
import com.notify.model.Mood;
import com.notify.repository.DiaryEntryRepository;
import com.notify.util.EncryptionUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiaryService {

    private final DiaryEntryRepository diaryEntryRepository;

    public DiaryService(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    public List<DiaryEntry> getRecentEntries(User user) {
        List<DiaryEntry> entries = diaryEntryRepository.findByUserOrderByCreatedDateDesc(user, PageRequest.of(0, 10));
        decryptEntries(entries);
        return entries;
    }

    public List<DiaryEntry> getAllEntries(User user) {
        List<DiaryEntry> entries = diaryEntryRepository.findByUserOrderByCreatedDateDesc(user);
        decryptEntries(entries);
        return entries;
    }

    // ✅ Correct param order: (LocalDate, User) for controller compatibility
    public List<DiaryEntry> getEntriesByDate(LocalDate date, User user) {
        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndEntryDate(user, date);
        decryptEntries(entries);
        return entries;
    }

    // Optional: Overloaded to maintain old usage
    public List<DiaryEntry> getEntriesByDate(User user, LocalDate date) {
        return getEntriesByDate(date, user);
    }

    public Optional<DiaryEntry> getEntryById(Long id, User user) {
        Optional<DiaryEntry> entry = diaryEntryRepository.findById(id);
        if (entry.isPresent() && entry.get().getUser().getId().equals(user.getId())) {
            DiaryEntry decrypted = decryptEntry(entry.get());
            return Optional.of(decrypted);
        }
        return Optional.empty();
    }

    public DiaryEntry saveEntry(DiaryEntry entry) {
        try {
            entry.setTitle(EncryptionUtil.encrypt(entry.getTitle()));
            entry.setContent(EncryptionUtil.encrypt(entry.getContent()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage());
        }
        return diaryEntryRepository.save(entry);
    }

    public void deleteEntry(Long id, User user) {
        Optional<DiaryEntry> entry = getEntryById(id, user);
        entry.ifPresent(diaryEntryRepository::delete);
    }

    public long getTotalEntries(User user) {
        return diaryEntryRepository.countByUser(user);
    }

    public long getEntriesThisMonth(User user) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return diaryEntryRepository.countByUserAndCreatedDateAfter(user, startOfMonth);
    }

    public String getLastEntryDate(User user) {
        List<DiaryEntry> latest = diaryEntryRepository.findTop1ByUserOrderByCreatedDateDesc(user);
        if (!latest.isEmpty()) {
            return latest.get(0).getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "Never";
    }

    private DiaryEntry decryptEntry(DiaryEntry entry) {
        try {
            entry.setTitle(EncryptionUtil.decrypt(entry.getTitle()));
            entry.setContent(EncryptionUtil.decrypt(entry.getContent()));
        } catch (Exception e) {
            System.err.println("Skipping entry ID " + entry.getId() + " due to decryption error: " + e.getMessage());
            entry.setTitle("[Decryption Failed]");
            entry.setContent("[Decryption Failed]");
        }
        return entry;
    }

    private void decryptEntries(List<DiaryEntry> entries) {
        for (DiaryEntry entry : entries) {
            decryptEntry(entry);
        }
    }

    public List<DiaryEntry> getRecentEntriesByMood(User user, String mood) {
        Mood moodEnum;
        try {
            moodEnum = Mood.valueOf(mood.toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndMoodOrderByCreatedDateDesc(user, moodEnum, PageRequest.of(0, 10));
        decryptEntries(entries);
        return entries;
    }

    public List<DiaryEntry> getAllEntriesByMood(User user, String mood) {
        Mood moodEnum;
        try {
            moodEnum = Mood.valueOf(mood.toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndMoodOrderByCreatedDateDesc(user, moodEnum);
        decryptEntries(entries);
        return entries;
    }

    public List<DiaryEntry> getEntriesByDateAndMood(User user, LocalDate date, String mood) {
        Mood moodEnum;
        try {
            moodEnum = Mood.valueOf(mood.toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndMoodAndCreatedDateBetweenOrderByCreatedDateDesc(
                user, moodEnum, startOfDay, endOfDay);
        decryptEntries(entries);
        return entries;
    }

    public Map<String, Long> getMoodStatistics(User user) {
        List<DiaryEntry> entries = diaryEntryRepository.findByUser(user);

        return entries.stream()
                .filter(entry -> entry.getMood() != null)
                .collect(Collectors.groupingBy(
                        entry -> entry.getMood().toString(),
                        Collectors.counting()
                ));
    }

    public Map<String, Map<String, Long>> getMonthlyMoodTrends(User user) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndCreatedDateAfterOrderByCreatedDateDesc(user, sixMonthsAgo);

        Map<String, Map<String, Long>> monthlyTrends = new HashMap<>();

        entries.stream()
                .filter(entry -> entry.getMood() != null)
                .forEach(entry -> {
                    String monthKey = entry.getCreatedDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    String mood = entry.getMood().toString();

                    monthlyTrends.computeIfAbsent(monthKey, k -> new HashMap<>())
                            .merge(mood, 1L, Long::sum);
                });

        return monthlyTrends;
    }

    public Map<String, String> getRecentMoodPattern(User user, int days) {
        LocalDateTime nDaysAgo = LocalDateTime.now().minusDays(days);
        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndCreatedDateAfterOrderByCreatedDateDesc(user, nDaysAgo);

        return entries.stream()
                .filter(entry -> entry.getMood() != null)
                .collect(Collectors.toMap(
                        entry -> entry.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM")),
                        entry -> entry.getMood().toString(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}
