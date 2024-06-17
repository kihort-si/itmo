package ru.itmo.client.controllers;

import ru.itmo.client.App;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.AuthorizationRequest;
import ru.itmo.common.network.response.AuthorizationResponse;
import ru.itmo.common.user.User;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class AuthorizationController implements Controller {
    private final ClientManager clientManager;
    private final User user;

    public AuthorizationController(ClientManager clientManager, User user) {
        this.clientManager = clientManager;
        this.user = user;
    }

    public int execute() {
        try {
            var response = (AuthorizationResponse) clientManager.sendAndReceiveCommand(new AuthorizationRequest(user.getLogin(), user.getPassword()));
            if (response.getStatus() == 1) return 1;
            if (response.getStatus() == 2) return 2;
            if (response.getStatus() == 3) return 3;
            if (response.getError() != null && !response.getError().isEmpty()) {
                throw new APIException(response.getError());
            }
            return response.getStatus();
        } catch (IOException | ClassNotFoundException e) {
            App.logger.error("при работе с сервером.");
            return 5;
        } catch (APIException e) {
            App.logger.error(e.getMessage());
            return 4;
        }
    }
}
