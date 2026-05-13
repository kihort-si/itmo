package ru.itmo.blps.app.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.itmo.blps.app.models.enums.UserRole;

public class AuthPrincipal implements UserDetails {
    private final String username;
    private final UserRole role;
    private final Long participantId;
    private final List<SimpleGrantedAuthority> authorities;

    public AuthPrincipal(String username, UserRole role, Long participantId, List<String> authorities) {
        this.username = username;
        this.role = role;
        this.participantId = participantId;
        this.authorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Long getParticipantId() {
        return participantId;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
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
