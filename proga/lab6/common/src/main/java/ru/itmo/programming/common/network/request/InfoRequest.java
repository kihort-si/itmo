package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class InfoRequest extends Request {
    public InfoRequest() {
        super(Commands.INFO.getName());
    }
}
