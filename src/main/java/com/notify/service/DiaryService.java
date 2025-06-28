package com.notify.service;

import com.notify.model.DiaryEntry;
import com.notify.model.User;
import com.notify.repository.DiaryEntryRepository;
import com.notify.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class DiaryService {

    @Autowired
    private DiaryEntryRepository diaryEntryRepository;

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

    public List<DiaryEntry> getEntriesByDate(User user, LocalDate date) {
        List<DiaryEntry> entries = diaryEntryRepository.findByUserAndEntryDate(user, date);
        decryptEntries(entries);
        return entries;
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

    // 🔓 Helper to decrypt a single entry safely
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

    // 🔓 Helper to decrypt a list of entries safely
    private void decryptEntries(List<DiaryEntry> entries) {
        for (DiaryEntry entry : entries) {
            decryptEntry(entry);
        }
    }
}
