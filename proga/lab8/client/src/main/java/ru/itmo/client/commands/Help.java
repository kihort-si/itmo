package ru.itmo.client.commands;

import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.util.Map;

/**
 * @author Nikita Vasilev
 */
public class Help extends Command {
    private final Console console;

    public Help(Console console) {
        super(Commands.HELP.getName(), Commands.HELP.getDescription());
        this.console = console;
    }

    @Override
    public boolean validateArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public void execute(String[] args) {
        if (!validateArgs(args)) {
            console.printError("У команды " + getName() + " не должно быть аргументов.");
        } else {
            console.println("Список доступных команд:");
            for (Map.Entry<String, String> entry : Commands.getCommands().entrySet()) {
                console.println("\u001B[3;32m" + entry.getKey() + ": " + entry.getValue() + "\u001B[0;0m");
            }
        }
    }
}
