package ru.itmo.programming.client.commands;


import ru.itmo.programming.client.builders.PersonBuilder;
import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.AddRequest;
import ru.itmo.programming.common.network.response.AddResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class Add extends Command {
    private final Console console;
    private final ClientManager clientManager;
    public Add(Console console, ClientManager clientManager) {
        super("add", "добавить новый элемент в коллекцию");
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
                var newPerson = (new PersonBuilder(console)).build();
                var response = (AddResponse) clientManager.sendAndReceiveCommand(new AddRequest(newPerson));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Человек с ID " + response.getNextId() + " успешно добавлен.");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}