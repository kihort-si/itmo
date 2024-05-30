package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class MaxByLocationRequest extends Request {
    public MaxByLocationRequest() {
        super(Commands.MAX_BY_LOCATION.getName());
    }
}
