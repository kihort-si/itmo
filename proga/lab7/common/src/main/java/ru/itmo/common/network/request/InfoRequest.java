package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class InfoRequest extends Request {
    public InfoRequest() {
        super(Commands.INFO.getName());
    }
}
