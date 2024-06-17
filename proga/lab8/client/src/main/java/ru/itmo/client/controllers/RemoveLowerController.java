package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.network.request.RemoveLowerRequest;
import ru.itmo.common.network.response.RemoveLowerResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class RemoveLowerController implements Controller {
    private final ClientManager clientManager;
    private final double height;

    public RemoveLowerController(ClientManager clientManager, double height) {
        this.clientManager = clientManager;
        this.height = height;
    }

    public int execute() {
        try {
            var response = (RemoveLowerResponse) clientManager.sendAndReceiveCommand(new RemoveLowerRequest(height));
            return response.getCount();
        } catch (IOException | ClassNotFoundException e) {
            return Integer.MAX_VALUE;
        }

    }
}
