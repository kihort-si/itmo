package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class ShowRequest extends Request {
    public ShowRequest() {
        super(Commands.SHOW.getName());
    }
}
