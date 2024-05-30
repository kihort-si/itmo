package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class ClearRequest extends Request {
    public ClearRequest() {
        super(Commands.CLEAR.getName());
    }
}
