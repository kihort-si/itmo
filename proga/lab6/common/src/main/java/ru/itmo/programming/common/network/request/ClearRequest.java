package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class ClearRequest extends Request {
    public ClearRequest() {
        super(Commands.CLEAR.getName());
    }
}
