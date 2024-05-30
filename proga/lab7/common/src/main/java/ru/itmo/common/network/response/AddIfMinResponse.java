package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class AddIfMinResponse extends Response {
    private final long nextId;
    private final boolean isAdded;
    public AddIfMinResponse(long nextId, boolean isAdded, String error) {
        super(Commands.ADD_IF_MIN.getName(), error);
        this.nextId = nextId;
        this.isAdded = isAdded;
    }

    public long getNextId() {
        return nextId;
    }

    public boolean isAdded() {
        return isAdded;
    }
}
