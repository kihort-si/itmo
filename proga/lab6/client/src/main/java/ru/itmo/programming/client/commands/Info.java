package ru.itmo.programming.client.commands;

import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.network.request.InfoRequest;
import ru.itmo.programming.common.network.response.InfoResponse;

import java.io.IOException;


/**
 * @author Nikita Vasilev
 */
public class Info extends Command {
    private final Console console;
    private final ClientManager clientManager;
    public Info(Console console, ClientManager clientManager) {
        super("info", "вывести в стандартный поток вывода информацию о коллекции");
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
                console.println(response.getInfoMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        }
    }
}
