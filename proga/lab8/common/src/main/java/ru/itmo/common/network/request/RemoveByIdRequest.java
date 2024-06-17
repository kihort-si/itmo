package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

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
