package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.ClearRequest;
import ru.itmo.common.network.response.ClearResponse;

import java.io.IOException;

public class ClearController implements Controller {
    private final ClientManager clientManager;

    public ClearController(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public int execute() {
        try {
            var response = (ClearResponse) clientManager.sendAndReceiveCommand(new ClearRequest());
            if (response.getError() != null && !response.getError().isEmpty()) {
                throw new APIException(response.getError());
            }
        } catch (IOException | ClassNotFoundException e) {
            return 1;
        } catch (APIException e) {
            return 2;
        }
        return 0;
    }
}
