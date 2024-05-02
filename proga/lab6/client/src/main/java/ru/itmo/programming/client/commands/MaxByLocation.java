package ru.itmo.programming.client.commands;

import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.network.request.MaxByLocationRequest;
import ru.itmo.programming.common.network.response.MaxByLocationResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class MaxByLocation extends Command {
    private final Console console;
    private final ClientManager clientManager;
    public MaxByLocation(Console console, ClientManager clientManager) {
        super("max_by_location", "вывести любой объект из коллекции, значение поля location которого является максимальным");
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
                var response = (MaxByLocationResponse) clientManager.sendAndReceiveCommand(new MaxByLocationRequest());
                console.println(response.getMaxByLocationPerson());
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        }
    }
}
