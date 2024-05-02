package ru.itmo.programming.client.commands;

import ru.itmo.programming.common.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class Exit extends Command {
    private final Console console;
    public Exit(Console console) {
        super("exit", "завершить программу (без сохранения в файл)");
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
            console.println("Завершение работы программы...");
            System.exit(1);
        }
    }
}
