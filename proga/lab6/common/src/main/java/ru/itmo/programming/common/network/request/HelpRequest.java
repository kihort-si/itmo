package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class HelpRequest extends Request {
    public HelpRequest() {
        super(Commands.HELP.getName());
    }
}
