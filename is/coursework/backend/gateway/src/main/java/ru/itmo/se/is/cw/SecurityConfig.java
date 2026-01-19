package ru.itmo.se.is.cw;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LogoutSuccessHandler logoutSuccessHandler,
            @Value("${app.spa-url}") String spaUrl
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/**", "/error").permitAll()
                        .requestMatchers(
                                "/resource/v3/api-docs", "/resource/v3/api-docs/**",
                                "/resource/v3/api-docs.yaml",
                                "/resource/swagger-ui.html", "/resource/swagger-ui/**",
                                "/resource/register",
                                "/resource/catalog", "/resource/catalog/**",
                                "/resource/files", "/resource/files/**",
                                "/resource/designs", "/resource/designs/**",
                                "/resource/materials", "/resource/materials/**"
                        ).permitAll()
                        .requestMatchers("/resource/**").authenticated()
                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> true
                        )
                )
                .oauth2Login(o -> o
                        .defaultSuccessUrl(spaUrl)
                )
                .oauth2Client(Customizer.withDefaults())
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                );

        return http.build();
    }
}
