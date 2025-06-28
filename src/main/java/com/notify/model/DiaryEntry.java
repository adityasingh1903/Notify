package com.notify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_entries")
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @NotNull(message = "Entry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    private Mood mood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public DiaryEntry() {
        this.createdDate = LocalDateTime.now();
        this.entryDate = LocalDate.now();
    }

    public DiaryEntry(String title, String content, User user) {
        this();
        this.title = title;
        this.content = content;
        this.user = user;
    }

    // Getters and setters...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public Mood getMood() { return mood; }
    public void setMood(Mood mood) { this.mood = mood; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
