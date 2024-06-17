package ru.itmo.client.controllers;

import ru.itmo.client.App;
import ru.itmo.client.builders.UserBuilder;
import ru.itmo.client.commands.Command;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.RegisterRequest;
import ru.itmo.common.network.response.RegisterResponse;
import ru.itmo.common.user.User;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class RegisterController implements Controller {
    private final ClientManager clientManager;
    private final User user;

    public RegisterController(ClientManager clientManager, User user) {
        this.clientManager = clientManager;
        this.user = user;
    }

    public int execute() {
        try {
            var response = (RegisterResponse) clientManager.sendAndReceiveCommand(new RegisterRequest(user));
            if (response.getStatus() == 1) return 1;
            if (response.getStatus() == 2) return 2;
            if (response.getError() != null && !response.getError().isEmpty()) {
                throw new APIException(response.getError());
            }
            return response.getStatus();
        } catch (IOException | ClassNotFoundException e) {
            App.logger.error("при работе с сервером.");
            return 4;
        } catch (APIException e) {
            App.logger.error(e.getMessage());
            return 3;
        }
    }
}
