package ru.itmo.se.is.cw.conf;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class JwtConfig {
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws IOException, JOSEException {
        ClassPathResource resource = new ClassPathResource("private_key.pem");
        String RSAPrivateKey = new String(resource.getInputStream().readAllBytes());
        RSAKey parsed = RSAKey.parseFromPEMEncodedObjects(RSAPrivateKey)
                .toRSAKey();
        RSAKey rsaKey = new RSAKey.Builder(parsed.toRSAPublicKey())
                .privateKey(parsed.toRSAPrivateKey())
                .keyID(parsed.getKeyID() != null ? parsed.getKeyID() : UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}
