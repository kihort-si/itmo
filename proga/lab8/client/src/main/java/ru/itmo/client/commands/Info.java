package ru.itmo.client.commands;

import ru.itmo.client.network.ClientManager;
import ru.itmo.common.exceptions.APIException;
import ru.itmo.common.network.request.InfoRequest;
import ru.itmo.common.network.response.InfoResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class Info extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public Info(Console console, ClientManager clientManager) {
        super(Commands.INFO.getName(), Commands.INFO.getDescription());
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
                var response = (InfoResponse) clientManager.sendAndReceiveCommand(new InfoRequest());
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println(response.getInfoMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
