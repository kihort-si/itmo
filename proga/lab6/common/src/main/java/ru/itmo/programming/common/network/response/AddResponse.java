package ru.itmo.programming.common.network.response;

import ru.itmo.programming.common.utils.Commands;

public class AddResponse extends Response {
    private final long nextId;
    public AddResponse(Long nextId, String error) {
        super(Commands.ADD.getName(), error);
        this.nextId = nextId;
    }

    public long getNextId() {
        return nextId;
    }
}
