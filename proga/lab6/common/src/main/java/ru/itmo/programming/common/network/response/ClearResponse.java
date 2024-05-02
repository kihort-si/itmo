package ru.itmo.programming.common.network.response;

import ru.itmo.programming.common.utils.Commands;

public class ClearResponse extends Response {
    public ClearResponse(String error) {
        super(Commands.CLEAR.getName(), error);
    }
}
