package ru.itmo.client.commands;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.AuthorizationRequest;
import ru.itmo.common.network.response.AuthorizationResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

public class Authorization extends Command {
    private final Console console;
    private final ClientManager clientManager;
    public Authorization(Console console, ClientManager clientManager) {
        super(Commands.AUTHORIZATION.getName(), Commands.AUTHORIZATION.getDescription());
        this.console = console;
        this.clientManager = clientManager;
    }

    @Override
    public boolean validateArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public void execute(String[] args) {
        if (!validateArgs(args)) {
            console.printError("У команды " + getName() + " не должно быть аргументов.");
        } else {
            console.println("Введите логин:");
            String login = console.readln();
            console.println("Введите пароль:");
            String password = console.passwordReader();

            try {
                var response = (AuthorizationResponse) clientManager.sendAndReceiveCommand(new AuthorizationRequest(login, password));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Вы вошли как " + login);
            } catch (IOException | ClassNotFoundException e) {
                console.printError("при работе с сервером.");
            } catch (APIException e) {
                console.printError(e.getMessage());
            }
        }
    }
}
