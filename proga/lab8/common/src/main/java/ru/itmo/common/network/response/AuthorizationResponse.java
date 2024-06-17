package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class AuthorizationResponse extends Response {
    private final String message;
    private final int status;

    public AuthorizationResponse(String message, String error, int status) {
        super(Commands.AUTHORIZATION.getName(), error, status);
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
