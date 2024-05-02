package ru.itmo.programming.client.commands;


import ru.itmo.programming.client.builders.PersonBuilder;
import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.UpdateIdRequest;
import ru.itmo.programming.common.network.response.UpdateIdResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class UpdateId extends Command {

    private final Console console;
    private final ClientManager clientManager;

    public UpdateId(Console console, ClientManager clientManager) {
        super("update_id", "обновить значение элемента коллекции, id которого равен заданному");
        this.console = console;
        this.clientManager = clientManager;
    }


    @Override
    public boolean validateArgs(String[] args) {
        return args.length == 1;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (!validateArgs(args)) {
                console.printError("У команды " + getName() + " не должно быть аргументов.");
            } else {
                long id = Long.parseLong(args[0]);
                var updatedPerson = (new PersonBuilder(console)).build();
                var response = (UpdateIdResponse) clientManager.sendAndReceiveCommand(new UpdateIdRequest(id, updatedPerson));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Человек с ID " + id + " успешно обновлен.");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
