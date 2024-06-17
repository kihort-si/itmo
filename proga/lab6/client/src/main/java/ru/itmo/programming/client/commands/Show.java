package ru.itmo.programming.client.commands;

import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.ShowRequest;
import ru.itmo.programming.common.network.response.ShowResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class Show extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public Show(Console console, ClientManager clientManager) {
        super(Commands.SHOW.getName(), Commands.SHOW.getDescription());
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
                var response = (ShowResponse) clientManager.sendAndReceiveCommand(new ShowRequest());
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }

                if (response.getPeople().isEmpty()) {
                    console.println("Коллекция пуста!");
                }

                for (var person : response.getPeople()) {
                    console.println(person);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
