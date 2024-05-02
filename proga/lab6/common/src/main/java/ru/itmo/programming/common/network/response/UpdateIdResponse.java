package ru.itmo.programming.common.network.response;

import ru.itmo.programming.common.utils.Commands;

public class UpdateIdResponse extends Response {
    public UpdateIdResponse(String error) {
        super(Commands.UPDATE_ID.getName(), error);
    }
}
