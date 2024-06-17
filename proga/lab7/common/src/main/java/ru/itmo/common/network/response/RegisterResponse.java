package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class RegisterResponse extends Response {
    private final String login;

    public RegisterResponse(String login, String error) {
        super(Commands.REGISTER.getName(), error);
        this.login = login;
    }

    public String getLogin() {
        return login;
    }
}
