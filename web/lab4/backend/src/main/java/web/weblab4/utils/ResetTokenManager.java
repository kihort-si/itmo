package web.weblab4.utils;

import jakarta.ejb.Stateless;

import java.util.UUID;

@Stateless
public class ResetTokenManager {
    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
