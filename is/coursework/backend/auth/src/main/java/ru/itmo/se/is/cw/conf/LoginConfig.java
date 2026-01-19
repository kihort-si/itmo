package ru.itmo.se.is.cw.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class LoginConfig {
    @Bean
    @Order(2)
    SecurityFilterChain jsonLoginChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/login.html", "/css/**", "/js/**", "/assets/**").permitAll()
                        .requestMatchers("/accounts/**").hasAuthority("SCOPE_internal")
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
