package ru.itmo.programming.client.commands;

import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.network.request.HelpRequest;
import ru.itmo.programming.common.network.response.HelpResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class Help extends Command {
    private final Console console;
    private final ClientManager clientManager;
    public Help(Console console, ClientManager clientManager) {
        super("help", "вывести справку по доступным командам");
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
                var response = (HelpResponse) clientManager.sendAndReceiveCommand(new HelpRequest());
                console.println(response.getHelpMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером");
        }
    }
}
