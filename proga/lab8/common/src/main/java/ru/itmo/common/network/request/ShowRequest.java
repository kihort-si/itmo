package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class ShowRequest extends Request {
    public ShowRequest() {
        super(Commands.SHOW.getName());
    }
}
