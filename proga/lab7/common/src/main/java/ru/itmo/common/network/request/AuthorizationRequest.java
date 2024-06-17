package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class AuthorizationRequest extends Request {
    private final String login;
    private final String password;

    public AuthorizationRequest(String login, String password) {
        super(Commands.AUTHORIZATION.getName());
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
