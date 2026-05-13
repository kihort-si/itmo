package ru.itmo.blps.app.security.jaas;

import java.security.Principal;

public record RolePrincipal(String name) implements Principal {
    @Override
    public String getName() {
        return name;
    }
}
