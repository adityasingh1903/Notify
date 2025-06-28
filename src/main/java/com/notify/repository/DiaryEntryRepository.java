package com.notify.repository;

import com.notify.model.DiaryEntry;
import com.notify.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByUserOrderByCreatedDateDesc(User user);

    List<DiaryEntry> findByUserAndEntryDate(User user, LocalDate entryDate);

    long countByUser(User user);  // ✅ Derived query, simpler

    long countByUserAndCreatedDateAfter(User user, LocalDateTime startOfMonth); // ✅ Derived query

    // ✅ Latest diary entry (top 1)
    List<DiaryEntry> findTop1ByUserOrderByCreatedDateDesc(User user);

    // ✅ Recent N entries using pageable
    List<DiaryEntry> findByUserOrderByCreatedDateDesc(User user, Pageable pageable);
}
