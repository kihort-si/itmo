package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class UpdateIdResponse extends Response {
    private final int status;
    public UpdateIdResponse(String error, int status) {
        super(Commands.UPDATE_ID.getName(), error, status);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
