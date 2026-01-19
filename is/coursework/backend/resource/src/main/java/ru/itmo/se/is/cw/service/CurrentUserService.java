package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.ClientUpdateRequestDto;
import ru.itmo.se.is.cw.dto.CurrentUserResponseDto;
import ru.itmo.se.is.cw.dto.ChangePasswordRequestDto;
import ru.itmo.se.is.cw.feign.AccountClient;
import ru.itmo.se.is.cw.mapper.ClientMapper;
import ru.itmo.se.is.cw.mapper.EmployeeMapper;
import ru.itmo.se.is.cw.model.ClientEntity;
import ru.itmo.se.is.cw.model.value.AccountRole;
import ru.itmo.se.is.cw.repository.ClientRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final ClientsService clientsService;
    private final ClientMapper clientMapper;
    private final EmployeesService employeesService;
    private final EmployeeMapper employeeMapper;
    private final ClientRepository clientRepository;
    private final AccountClient accountClient;

    @Transactional
    public CurrentUserResponseDto getCurrentUser() {
        CurrentUserResponseDto currentUserResponseDto = new CurrentUserResponseDto();
        Long accountId = getAccountId();
        currentUserResponseDto.setAccountId(accountId);
        currentUserResponseDto.setUsername(getUsername());
        currentUserResponseDto.setRole(getRole());
        if (hasRole(AccountRole.CLIENT)) {
            currentUserResponseDto.setClient(
                    clientMapper.toDto(
                            clientsService.getByAccountId(accountId)
                    )
            );
            return currentUserResponseDto;
        }
        if (hasRole(AccountRole.ADMIN)) {
            currentUserResponseDto.setRole(AccountRole.ADMIN);
            return currentUserResponseDto;
        }
        currentUserResponseDto.setEmployee(
                employeeMapper.toDto(
                        employeesService.getByAccountId(accountId)
                )
        );
        return currentUserResponseDto;
    }

    public Long getAccountId() {
        JwtAuthenticationToken jwtAuth = jwtAuth();
        Object claim = jwtAuth.getToken().getClaim("account_id");
        if (claim instanceof Number n) return n.longValue();
        if (claim instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("Missing/invalid account_id claim");
    }

    public String getUsername() {
        JwtAuthenticationToken jwtAuth = jwtAuth();
        return jwtAuth.getToken().getSubject();
    }

    public AccountRole getRole() {
        return jwtAuth().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(r -> r.substring("ROLE_".length()))
                .map(AccountRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown role"));
    }

    public boolean hasRole(AccountRole role) {
        return role.equals(getRole());
    }

    private JwtAuthenticationToken jwtAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No JWT authentication");
        }
        return jwtAuth;
    }

    @Transactional
    public void updateCurrentUser(ClientUpdateRequestDto request) {
        ClientEntity client = clientsService.getByAccountId(getAccountId());
        clientMapper.updateEntity(request, client);
        clientRepository.save(client);
    }

    public void changePassword(ChangePasswordRequestDto request) {
        accountClient.changePassword(getAccountId(), request);
    }
}
