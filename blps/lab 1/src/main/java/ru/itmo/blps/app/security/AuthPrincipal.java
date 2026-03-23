package ru.itmo.blps.app.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.itmo.blps.app.models.AppUser;

public class AuthPrincipal implements UserDetails {
    private final Long userId;
    private final String username;
    private final String role;
    private final Long participantId;

    public AuthPrincipal(AppUser user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().name();
        this.participantId = user.getParticipantId();
    }

    public AuthPrincipal(Long userId, String username, String role, Long participantId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.participantId = participantId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
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
