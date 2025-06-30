package com.notify.repository;

import com.notify.model.DiaryEntry;
import com.notify.model.Mood;
import com.notify.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    // ✅ All entries ordered
    List<DiaryEntry> findByUserOrderByCreatedDateDesc(User user);

    // ✅ By date
    List<DiaryEntry> findByUserAndEntryDate(User user, LocalDate entryDate);

    // ✅ Count total
    long countByUser(User user);

    // ✅ Count this month
    long countByUserAndCreatedDateAfter(User user, LocalDateTime startOfMonth);

    // ✅ Latest diary entry (top 1)
    List<DiaryEntry> findTop1ByUserOrderByCreatedDateDesc(User user);

    // ✅ Recent N entries using pageable
    List<DiaryEntry> findByUserOrderByCreatedDateDesc(User user, Pageable pageable);

    // ✅ Find entries by user and mood with pagination
    List<DiaryEntry> findByUserAndMoodOrderByCreatedDateDesc(User user, Mood mood, Pageable pageable);

    // ✅ Find all entries by user and mood
    List<DiaryEntry> findByUserAndMoodOrderByCreatedDateDesc(User user, Mood mood);

    // ✅ Find entries by user and mood within date range
    List<DiaryEntry> findByUserAndMoodAndCreatedDateBetweenOrderByCreatedDateDesc(
            User user, Mood mood, LocalDateTime startDate, LocalDateTime endDate);

    // ✅ Find entries by user after a specific date
    List<DiaryEntry> findByUserAndCreatedDateAfterOrderByCreatedDateDesc(User user, LocalDateTime date);

    // ✅ Find all entries by user (without ordering for statistics)
    List<DiaryEntry> findByUser(User user);
}
