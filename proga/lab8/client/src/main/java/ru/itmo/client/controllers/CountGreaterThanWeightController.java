package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.network.request.CountGreaterThanWeightRequest;
import ru.itmo.common.network.response.CountGreaterThanWeightResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class CountGreaterThanWeightController implements Controller {
    private final ClientManager clientManager;
    private final double height;

    public CountGreaterThanWeightController(ClientManager clientManager, double height) {
        this.clientManager = clientManager;
        this.height = height;
    }

    public int execute() {
        try {
            var response = (CountGreaterThanWeightResponse) clientManager.sendAndReceiveCommand(new CountGreaterThanWeightRequest(height));
            return response.getCount();
        } catch (IOException | ClassNotFoundException e) {
            return Integer.MAX_VALUE;
        }

    }
}
