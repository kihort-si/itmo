package ru.itmo.programming.commands;

import ru.itmo.programming.managers.CommandManager;
import ru.itmo.programming.utils.Console;

import java.util.Map;

/**
 * @author Nikita Vasilev
 */
public class Help extends Command {
    private final Console console;
    private final CommandManager commandManager;
    public Help(Console console, CommandManager commandManager) {
        super("help", "вывести справку по доступным командам");
        this.commandManager = commandManager;
        this.console = console;
    }

    @Override
    public boolean validate(String[] args) {
        return args.length == 0;
    }

    @Override
    public void execute(String[] args) {
        if (!validate(args)) {
            console.printError("У команды " + getName() + " не должно быть аргумента");
        } else {
            console.println("Список доступных команд:");
            for (Map.Entry<String, Command> entry : commandManager.getCommandMap().entrySet()) {
                console.println("\u001B[3;32m" + entry.getValue() + "\u001B[0;0m");
            }
        }
    }
}
