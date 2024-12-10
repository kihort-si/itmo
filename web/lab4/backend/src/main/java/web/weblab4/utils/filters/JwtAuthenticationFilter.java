package web.weblab4.utils.filters;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        if (path.contains("users/register") || path.contains("users/login") || path.contains("users/reset")) {
            return;
        }

        String authHeader = requestContext.getHeaderString(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Authorization header missing").build());
            return;
        }

        String secret = System.getenv("TOKEN_SECRET");
        if (secret == null) {
            throw new IllegalStateException("TOKEN_SECRET not set");
        }

        byte[] decodedSecret = Base64.getDecoder().decode(secret);
        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(decodedSecret)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String tokenType = claims.get("type", String.class);

            if (username == null || tokenType == null) {
                throw new SecurityException("Invalid token structure");
            }

            if (path.contains("users/refresh")) {
                if (!"refresh".equals(tokenType)) {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Expected refresh token").build());
                }
                return;
            }

            if ("access".equals(tokenType)) {
                SecurityContext originalContext = requestContext.getSecurityContext();
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> username;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return false;
                    }

                    @Override
                    public boolean isSecure() {
                        return originalContext.isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "Bearer";
                    }
                });
                return;
            }

            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token type").build());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            if (!path.contains("users/refresh")) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Access token expired").build());
            }
        } catch (io.jsonwebtoken.SignatureException e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token signature").build());
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build());
        }
    }
}
