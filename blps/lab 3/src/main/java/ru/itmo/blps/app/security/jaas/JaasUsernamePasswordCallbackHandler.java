package ru.itmo.blps.app.security.jaas;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class JaasUsernamePasswordCallbackHandler implements CallbackHandler {
    private final String username;
    private final String password;

    public JaasUsernamePasswordCallbackHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback nameCallback) {
                nameCallback.setName(username);
                continue;
            }
            if (callback instanceof PasswordCallback passwordCallback) {
                passwordCallback.setPassword(password == null ? new char[0] : password.toCharArray());
                continue;
            }
            throw new UnsupportedCallbackException(callback);
        }
    }
}
