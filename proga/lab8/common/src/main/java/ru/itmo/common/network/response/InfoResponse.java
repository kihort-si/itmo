package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class InfoResponse extends Response {
    private final String[] infoMessage;
    public InfoResponse(String[] infoMessage, String error, int status) {
        super(Commands.INFO.getName(), error, status);
        this.infoMessage = infoMessage;
    }

    public String[] getInfoMessage() {
        return infoMessage;
    }
}
