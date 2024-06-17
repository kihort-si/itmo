package ru.itmo.client.controllers;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.UpdateIdRequest;
import ru.itmo.common.network.response.UpdateIdResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class UpdateController implements Controller {
    private final ClientManager clientManager;
    private final Person updatedPerson;
    private final long id;

    public UpdateController(ClientManager clientManager, Person updatedPerson, long id) {
        this.clientManager = clientManager;
        this.updatedPerson = updatedPerson;
        this.id = id;
    }

    public int execute() {
        try {
            var response = (UpdateIdResponse) clientManager.sendAndReceiveCommand(new UpdateIdRequest(id, updatedPerson));
            return response.getStatus();
        } catch (IOException | ClassNotFoundException e) {
            return 5;
        }
    }
}
