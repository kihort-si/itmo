package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.model.AccountEntity;
import ru.itmo.se.is.cw.model.value.AccountRole;
import ru.itmo.se.is.cw.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AdminInitService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private String username = "admin";

    private String password = "admin";

    @Transactional
    public void ensureAdminExists() {
        if (accountRepository.existsByUsername(username)) {
            return;
        }

        AccountEntity admin = new AccountEntity();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEnabled(true);
        admin.setRole(AccountRole.ADMIN);

        accountRepository.save(admin);
    }

}
