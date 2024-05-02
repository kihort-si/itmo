package ru.itmo.programming.common.network.response;

import ru.itmo.programming.common.utils.Commands;

public class InfoResponse extends Response {
    private final String infoMessage;
    public InfoResponse(String infoMessage, String error) {
        super(Commands.INFO.getName(), error);
        this.infoMessage = infoMessage;
    }

    public String getInfoMessage() {
        return infoMessage;
    }
}
