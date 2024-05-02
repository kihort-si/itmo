package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.collection.Location;
import ru.itmo.programming.common.utils.Commands;

public class MaxByLocationRequest extends Request {
    public MaxByLocationRequest() {
        super(Commands.MAX_BY_LOCATION.getName());
    }
}
