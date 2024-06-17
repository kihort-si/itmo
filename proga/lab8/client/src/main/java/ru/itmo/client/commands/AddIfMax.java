package ru.itmo.client.commands;

import ru.itmo.client.builders.PersonBuilder;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.AddIfMaxRequest;
import ru.itmo.common.network.response.AddIfMaxResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class AddIfMax extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public AddIfMax(Console console, ClientManager clientManager) {
        super(Commands.ADD_IF_MAX.getName(), Commands.ADD_IF_MAX.getDescription());
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
                var response = (AddIfMaxResponse) clientManager.sendAndReceiveCommand(new AddIfMaxRequest(newPerson));
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
