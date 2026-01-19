package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.se.is.cw.dto.AccountRequestDto;
import ru.itmo.se.is.cw.dto.AccountResponseDto;
import ru.itmo.se.is.cw.dto.ChangePasswordRequestDto;
import ru.itmo.se.is.cw.dto.VerifyEmailRequestDto;
import ru.itmo.se.is.cw.mapper.AccountMapper;
import ru.itmo.se.is.cw.model.AccountEntity;
import ru.itmo.se.is.cw.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto request) {
        if (accountRepository.existsByUsername(request.getUsername()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username exists");
        AccountEntity account = accountMapper.toEntity(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEnabled(false);
        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDto request) {
        AccountEntity account = getById(id);
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Old password is incorrect");
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Transactional
    public void enableAccount(Long id) {
        AccountEntity account = getById(id);
        account.setEnabled(true);
        accountRepository.save(account);
    }

    @Transactional
    public void disableAccount(Long id) {
        AccountEntity account = getById(id);
        account.setEnabled(false);
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public AccountEntity getById(Long id) {
        return accountRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with id " + id + " not found")
                );
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getAccount(Long id) {
        AccountEntity account = getById(id);
        AccountResponseDto dto = accountMapper.toDto(account);
        System.out.println("AuthService.getAccount: id=" + id + ", username=" + dto.getUsername() + ", enabled=" + dto.getEnabled());
        return dto;
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequestDto request) {
        // TODO: проверить токен, активировать аккаунт
    }
}
