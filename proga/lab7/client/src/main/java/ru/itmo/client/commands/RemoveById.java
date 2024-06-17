package ru.itmo.client.commands;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.RemoveByIdRequest;
import ru.itmo.common.network.response.RemoveByIdResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class RemoveById extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public RemoveById(Console console, ClientManager clientManager) {
        super(Commands.REMOVE_BY_ID.getName(), Commands.REMOVE_BY_ID.getDescription());
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
                console.printError("У команды " + getName() + " должен быть аргумент.");
                console.printError("Введите ID элемента, который необходимо удалить.");
            } else {
                long id = Long.parseLong(args[0]);
                var response = (RemoveByIdResponse) clientManager.sendAndReceiveCommand(new RemoveByIdRequest(id));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Человек с ID " + id + " успешно удалён.");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
