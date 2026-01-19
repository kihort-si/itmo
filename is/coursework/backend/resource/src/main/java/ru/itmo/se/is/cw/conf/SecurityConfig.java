package ru.itmo.se.is.cw.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/v3/api-docs", "/v3/api-docs/**",
                            "/v3/api-docs.yaml", "/swagger-ui/**"
                    ).permitAll()
                    .requestMatchers("/register").permitAll()
                    .requestMatchers("/catalog", "/catalog/**").permitAll()
                    .requestMatchers("/files", "/files/**").permitAll()
                    .requestMatchers("/designs", "/designs/**").permitAll()
                    .requestMatchers("/materials", "/materials/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}

