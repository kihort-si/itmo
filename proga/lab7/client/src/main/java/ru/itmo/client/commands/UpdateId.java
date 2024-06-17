package ru.itmo.client.commands;

import ru.itmo.client.builders.PersonBuilder;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.UpdateIdRequest;
import ru.itmo.common.network.response.UpdateIdResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class UpdateId extends Command {

    private final Console console;
    private final ClientManager clientManager;

    public UpdateId(Console console, ClientManager clientManager) {
        super(Commands.UPDATE_ID.getName(), Commands.UPDATE_ID.getDescription());
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
