package ru.itmo.se.is.cw;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class ManualOidcLogoutSuccessHandler implements LogoutSuccessHandler {

    private final String endSessionEndpoint;
    private final String spaUrl;

    public ManualOidcLogoutSuccessHandler(
            @Value("${app.end-session-endpoint}") String endSessionEndpoint,
            @Value("${app.spa-url}") String spaUrl
    ) {
        this.endSessionEndpoint = endSessionEndpoint;
        this.spaUrl = spaUrl;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        if (authentication instanceof OAuth2AuthenticationToken oat
                && oat.getPrincipal() instanceof OidcUser oidcUser
                && oidcUser.getIdToken() != null) {

            String redirect = UriComponentsBuilder
                    .fromUriString(endSessionEndpoint)
                    .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
                    .queryParam("post_logout_redirect_uri", spaUrl)
                    .build(true)
                    .toUriString();

            response.sendRedirect(redirect);
            return;
        }
        response.sendRedirect(spaUrl);
    }
}
