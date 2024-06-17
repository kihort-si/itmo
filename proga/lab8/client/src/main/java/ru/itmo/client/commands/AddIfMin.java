package ru.itmo.client.commands;

import ru.itmo.client.builders.PersonBuilder;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.AddIfMinRequest;
import ru.itmo.common.network.response.AddIfMinResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class AddIfMin extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public AddIfMin(Console console, ClientManager clientManager) {
        super(Commands.ADD_IF_MIN.getName(), Commands.ADD_IF_MIN.getDescription());
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
                var response = (AddIfMinResponse) clientManager.sendAndReceiveCommand(new AddIfMinRequest(newPerson));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                } else if (!response.isAdded()) {
                    console.println(response.getError());
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