package ru.itmo.se.is.cw.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import ru.itmo.se.is.cw.model.value.AccountRole;

@RequiredArgsConstructor
public class GrantedAuthorityAdapter implements GrantedAuthority {

    private final AccountRole role;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.role.name();
    }
}
