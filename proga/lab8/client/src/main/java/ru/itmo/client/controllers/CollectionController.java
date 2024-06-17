package ru.itmo.client.controllers;

import ru.itmo.client.App;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.ShowRequest;
import ru.itmo.common.network.response.ShowResponse;

import java.io.IOException;
import java.util.List;

public class CollectionController {
    private final ClientManager clientManager;

    public CollectionController(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public List<Person> execute() {
        try {
            var response = (ShowResponse) clientManager.sendAndReceiveCommand(new ShowRequest());
            return response.getPeople();
        } catch (IOException | ClassNotFoundException e) {
            App.logger.error("Ошибка при работе с сервером.");
            return null;
        }
    }
}
