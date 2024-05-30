package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class RemoveByIdResponse extends Response {

    public RemoveByIdResponse(String error) {
        super(Commands.REMOVE_BY_ID.getName(), error);
    }
}
