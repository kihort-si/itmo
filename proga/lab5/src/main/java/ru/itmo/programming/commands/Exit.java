package ru.itmo.programming.commands;

import ru.itmo.programming.utils.Console;

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
    public void execute(String[] args) {
        console.println("Завершение работы программы");
        System.exit(0);
    }
}
