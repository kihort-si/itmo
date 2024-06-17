package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.network.request.RemoveByIdRequest;
import ru.itmo.common.network.response.RemoveByIdResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class RemoveController implements Controller {
    private final ClientManager clientManager;
    private final int id;

    public RemoveController(ClientManager clientManager, int id) {
        this.clientManager = clientManager;
        this.id = id;
    }

    public int execute() {
        try {
            var response = (RemoveByIdResponse) clientManager.sendAndReceiveCommand(new RemoveByIdRequest(id));
            return response.getStatus();
        } catch (IOException | ClassNotFoundException e) {
            return 5;
        }
    }
}
