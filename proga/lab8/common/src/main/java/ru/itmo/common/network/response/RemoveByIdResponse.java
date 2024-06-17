package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class RemoveByIdResponse extends Response {
    private final int status;

    public RemoveByIdResponse(String error, int status) {
        super(Commands.REMOVE_BY_ID.getName(), error, status);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
