package ru.itmo.client.commands;

import ru.itmo.client.builders.UserBuilder;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.RegisterRequest;
import ru.itmo.common.network.response.RegisterResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

public class Register extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public Register(Console console, ClientManager clientManager) {
        super(Commands.REGISTER.getName(), Commands.REGISTER.getDescription());
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
            try {
                var newUser = (new UserBuilder(console).build());
                var response = (RegisterResponse) clientManager.sendAndReceiveCommand(new RegisterRequest(newUser));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                } else {
                    console.println("Пользователь " + response.getLogin() + " успешно зарегистрирован");
                }
            } catch (IOException | ClassNotFoundException e) {
                console.printError("при работе с сервером.");
            } catch (APIException e) {
                console.printError(e.getMessage());
            }
        }
    }
}
