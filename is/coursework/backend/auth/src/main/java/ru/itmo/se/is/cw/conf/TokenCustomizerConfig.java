package ru.itmo.se.is.cw.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import ru.itmo.se.is.cw.adapter.UserDetailsAdapter;

import java.util.List;

@Configuration
public class TokenCustomizerConfig {
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return ctx -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(ctx.getTokenType())) {
                return;
            }

            List<String> authorities = ctx.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            ctx.getClaims().claim("roles", authorities);

            Object principal = ctx.getPrincipal().getPrincipal();
            if (principal instanceof UserDetailsAdapter uda) {
                ctx.getClaims().claim("account_id", uda.getAccountId());
            }
        };
    }
}
