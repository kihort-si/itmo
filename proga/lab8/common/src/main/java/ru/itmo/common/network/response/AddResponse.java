package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class AddResponse extends Response {
    private final long nextId;
    public AddResponse(long nextId, String error, int status) {
        super(Commands.ADD.getName(), error, status);
        this.nextId = nextId;
    }

    public long getNextId() {
        return nextId;
    }
}
