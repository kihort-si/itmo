package ru.itmo.se.is.cw.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class JwtConf {

    private static final Map<String, Set<String>> ROLE_TO_SCOPES = Map.of(
            "CLIENT", Set.of(
                    "applications.read",
                    "applications.write",
                    "applications.attachments.read",
                    "applications.attachments.write",
                    "conversations.messages.read",
                    "conversations.messages.write",
                    "orders.conversation.read",
                    "orders.read",
                    "files.read",
                    "designs.read",
                    "materials.read",
                    "files.write",
                    "catalog.read",
                    "clients.read",
                    "employees.read",
                    "orders.status.clint-approve",
                    "orders.status.clint-deny"
            ),
            "SALES_MANAGER", Set.of(
                    "orders.read",
                    "orders.write",
                    "orders.status.write",
                    "orders.price.write",
                    "clients.read",
                    "applications.read",
                    "applications.attachments.read",
                    "conversations.messages.read",
                    "conversations.messages.write",
                    "orders.conversation.read",
                    "files.read",
                    "files.write",
                    "employees.read",
                    "catalog.write",
                    "catalog.delete",
                    "designs.read"
            ),
            "CONSTRUCTOR", Set.of(
                    "designs.read",
                    "orders.status.write",
                    "orders.price.write",
                    "designs.write",
                    "designs.delete",
                    "orders.read",
                    "materials.read",
                    "applications.read",
                    "applications.attachments.read",
                    "files.read",
                    "files.write",
                    "conversations.messages.read",
                    "conversations.messages.write",
                    "orders.conversation.read",
                    "clients.read"
            ),
            "CNC_OPERATOR", Set.of(
                    "production.read",
                    "production.execute",
                    "materials.read",
                    "orders.read",
                    "orders.status.write",
                    "files.read",
                    "designs.read"
            ),
            "WAREHOUSE_WORKER", Set.of(
                    "materials.read",
                    "materials.balance.write",
                    "po.read",
                    "po.receive",
                    "orders.read",
                    "orders.status.write",
                    "designs.read",
                    "files.read",
                    "clients.read",
                    "applications.read"
            ),
            "SUPPLY_MANAGER", Set.of(
                    "materials.read",
                    "materials.write",
                    "materials.balance.write",
                    "orders.read",
                    "orders.materials.read",
                    "po.read",
                    "po.write",
                    "po.receive",
                    "catalog.read"
            ),
            "ADMIN", Set.of(
                    "materials.read", "materials.write", "materials.balance.write", "materials.delete",
                    "catalog.read", "catalog.write", "catalog.delete",
                    "po.read", "po.write", "po.receive",
                    "production.read", "production.execute",
                    "orders.read", "orders.write", "orders.status.write", "orders.price.write", "orders.conversation.read", "orders.materials.read", "orders.status.clint-approve",
                    "clients.read",
                    "designs.read", "designs.write", "designs.delete",
                    "employees.read", "employees.write",
                    "conversations.messages.read", "conversations.messages.write", "conversations.patricipants.read",
                    "files.read", "files.write", "files.delete",
                    "applications.read", "applications.write",
                    "applications.attachments.read", "applications.attachments.write"
            )
    );

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter base = new JwtGrantedAuthoritiesConverter();
        base.setAuthoritiesClaimName("roles");
        base.setAuthorityPrefix("");

        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> roles = new HashSet<>(base.convert(jwt));

            Set<GrantedAuthority> scopes = roles.stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(roleName -> roleName.substring("ROLE_".length()))
                    .flatMap(roleName -> ROLE_TO_SCOPES.getOrDefault(roleName, Set.of()).stream())
                    .map(scopeName -> "SCOPE_" + scopeName)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

            return Stream
                    .concat(roles.stream(), scopes.stream())
                    .collect(Collectors.toSet());
        });

        return conv;
    }
}
