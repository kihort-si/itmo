package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.MaxByLocationRequest;
import ru.itmo.common.network.response.MaxByLocationResponse;

import java.io.IOException;

public class MaxByLocationController {
    private final ClientManager clientManager;

    public MaxByLocationController(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public Person execute() {
        try {
            var response = (MaxByLocationResponse) clientManager.sendAndReceiveCommand(new MaxByLocationRequest());
            return response.getMaxByLocationPerson();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

    }
}
