package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class AddResponse extends Response {
    private final long nextId;
    public AddResponse(long nextId, String error) {
        super(Commands.ADD.getName(), error);
        this.nextId = nextId;
    }

    public long getNextId() {
        return nextId;
    }
}
