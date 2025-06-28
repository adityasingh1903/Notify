package com.notify.security;

import com.notify.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // ✅ Used in @AuthenticationPrincipal to get full User object
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // You can add roles later — for now return ROLE_USER
        return Collections.singleton(() -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Hashed password
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Login using email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
