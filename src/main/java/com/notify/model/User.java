package com.notify.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "notify_user") // 💡 table name lowercase for consistency
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ correct for auto-increment
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryEntry> entries;

    // Constructors
    public User() {}

    public User(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<DiaryEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<DiaryEntry> entries) {
        this.entries = entries;
    }
}
