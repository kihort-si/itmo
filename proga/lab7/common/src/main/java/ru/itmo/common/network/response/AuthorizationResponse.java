package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class AuthorizationResponse extends Response {
    private final String message;
    public AuthorizationResponse(String message, String error) {
        super(Commands.AUTHORIZATION.getName(), error);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
