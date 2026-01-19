package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.itmo.se.is.cw.adapter.UserDetailsAdapter;
import ru.itmo.se.is.cw.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository
                .findByUsername(username)
                .map(UserDetailsAdapter::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " was not found"));
    }
}
