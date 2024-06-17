package ru.itmo.programming.client.commands;

import ru.itmo.programming.client.builders.PersonBuilder;
import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.AddIfMinRequest;
import ru.itmo.programming.common.network.response.AddIfMinResponse;

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