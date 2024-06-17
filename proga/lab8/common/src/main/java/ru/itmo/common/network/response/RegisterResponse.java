package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class RegisterResponse extends Response {
    private final String login;
    private final int status;
    public RegisterResponse(String login, String error, int status) {
        super(Commands.REGISTER.getName(), error, status);
        this.login = login;
        this.status = status;
    }

    public String getLogin() {
        return login;
    }

    public int getStatus() {
        return status;
    }
}
