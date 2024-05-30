package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class ClearResponse extends Response {
    public ClearResponse(String error) {
        super(Commands.CLEAR.getName(), error);
    }
}
