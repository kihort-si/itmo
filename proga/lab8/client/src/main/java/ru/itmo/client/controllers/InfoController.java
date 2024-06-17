package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.network.request.InfoRequest;
import ru.itmo.common.network.response.InfoResponse;

import java.io.IOException;

public class InfoController {
    private final ClientManager clientManager;

    public InfoController(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public String[] execute() {
        try {
            var response = (InfoResponse) clientManager.sendAndReceiveCommand(new InfoRequest());
            return response.getInfoMessage();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
