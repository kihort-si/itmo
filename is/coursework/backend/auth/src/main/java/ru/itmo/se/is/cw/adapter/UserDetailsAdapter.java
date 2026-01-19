package ru.itmo.se.is.cw.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.itmo.se.is.cw.model.AccountEntity;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class UserDetailsAdapter implements UserDetails {
    private final AccountEntity account;

    public Long getAccountId() {
        return account.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(this.account.getRole())
                .map(GrantedAuthorityAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.account.getPassword();
    }

    @Override
    public String getUsername() {
        return this.account.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return this.account.getEnabled();
    }
}
