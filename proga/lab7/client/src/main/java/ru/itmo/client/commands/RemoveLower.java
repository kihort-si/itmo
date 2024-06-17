package ru.itmo.client.commands;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.RemoveLowerRequest;
import ru.itmo.common.network.response.RemoveLowerResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class RemoveLower extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public RemoveLower(Console console, ClientManager clientManager) {
        super(Commands.REMOVE_LOWER.getName(), Commands.REMOVE_LOWER.getDescription());
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
                console.printError("Введите рост, людей меньше которого нужно удалить.");
            } else {
                double height = Double.parseDouble(args[0]);
                var response = (RemoveLowerResponse) clientManager.sendAndReceiveCommand(new RemoveLowerRequest(height));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Люди с ростом меньше " + height + " успешно удалены.");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
