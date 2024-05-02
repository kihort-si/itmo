package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class RemoveByIdRequest extends Request {
    private final long id;
    public RemoveByIdRequest(long id) {
        super(Commands.REMOVE_BY_ID.getName());
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
