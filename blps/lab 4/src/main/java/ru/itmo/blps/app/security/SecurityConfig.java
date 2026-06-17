package ru.itmo.blps.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JaasAuthenticationProvider jaasAuthenticationProvider;
    private final String realmName;

    public SecurityConfig(JaasAuthenticationProvider jaasAuthenticationProvider,
                          @Value("${app.security.basic.realm}") String realmName) {
        this.jaasAuthenticationProvider = jaasAuthenticationProvider;
        this.realmName = realmName;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(jaasAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                       .requestMatchers("/api/auth/**").permitAll()
                       .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                       .requestMatchers(HttpMethod.GET, "/api/catalog/products", "/api/catalog/shops")
                       .hasAuthority("CATALOG_READ")
                       .requestMatchers(HttpMethod.POST, "/api/catalog/products")
                       .hasAuthority("PRODUCT_MANAGE")
                       .requestMatchers(HttpMethod.PUT, "/api/catalog/products/*")
                       .hasAuthority("PRODUCT_MANAGE")
                       .requestMatchers(HttpMethod.PATCH, "/api/catalog/products/*")
                       .hasAuthority("PRODUCT_MANAGE")
                       .requestMatchers(HttpMethod.DELETE, "/api/catalog/products/*")
                       .hasAuthority("PRODUCT_MANAGE")
                       .requestMatchers(HttpMethod.POST, "/api/catalog/shops")
                       .hasAuthority("SHOP_MANAGE")
                       .requestMatchers(HttpMethod.PUT, "/api/catalog/shops/*")
                       .hasAuthority("SHOP_MANAGE")
                       .requestMatchers(HttpMethod.DELETE, "/api/catalog/shops/*")
                       .hasAuthority("SHOP_MANAGE")
                       .requestMatchers(HttpMethod.POST, "/api/catalog/promo-codes")
                       .hasAuthority("PROMO_MANAGE")
                       .requestMatchers("/api/customers/*/cart/**")
                       .hasAuthority("CART_MANAGE")
                       .requestMatchers(HttpMethod.GET, "/api/customers/me")
                       .hasAuthority("CUSTOMER_PROFILE_READ")
                       .requestMatchers(HttpMethod.POST, "/api/customers/*/orders/checkout")
                       .hasAuthority("ORDER_CHECKOUT")
                       .requestMatchers(HttpMethod.GET, "/api/customers/*/orders")
                       .hasAuthority("ORDER_READ_OWN")
                       .requestMatchers(HttpMethod.GET, "/api/orders/*")
                       .hasAnyAuthority("ORDER_READ_OWN", "ORDER_READ_ASSIGNED")
                       .requestMatchers(HttpMethod.POST, "/api/orders/*/ready-for-pickup")
                       .hasAuthority("ORDER_PROCESS_PICKUP")
                       .requestMatchers(HttpMethod.POST, "/api/orders/*/assistant/complete-pickup")
                       .hasAuthority("ORDER_PROCESS_PICKUP")
                       .requestMatchers(HttpMethod.POST, "/api/orders/*/ready-for-delivery")
                       .hasAuthority("ORDER_PROCESS_DELIVERY")
                       .requestMatchers(HttpMethod.POST, "/api/orders/*/courier/delivered")
                       .hasAuthority("ORDER_PROCESS_DELIVERY")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
                            response.getWriter().write("{\"error\":\"Требуется авторизация\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"Доступ запрещен\"}");
                        })
                )
        ;

        return http.build();
    }
}
