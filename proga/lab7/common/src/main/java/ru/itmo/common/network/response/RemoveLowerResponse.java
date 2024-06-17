package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class RemoveLowerResponse extends Response {
    private final int count;

    public RemoveLowerResponse(Integer count, String error) {
        super(Commands.REMOVE_LOWER.getName(), error);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
