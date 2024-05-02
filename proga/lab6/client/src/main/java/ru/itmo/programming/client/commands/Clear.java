package ru.itmo.programming.client.commands;


import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.ClearRequest;
import ru.itmo.programming.common.network.response.ClearResponse;

import java.io.IOException;

/**
 * @author Nikita
 */
public class Clear extends Command {
    private final Console console;
    private final ClientManager clientManager;
    public Clear(Console console, ClientManager clientManager) {
        super("clear", "очистить коллекцию");
        this.console = console;
        this.clientManager = clientManager;
    }

    @Override
    public boolean validateArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (!validateArgs(args)) {
                console.printError("У команды " + getName() + " не должно быть аргументов.");
            } else {
                var response = (ClearResponse) clientManager.sendAndReceiveCommand(new ClearRequest());
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Коллекция очищена");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
