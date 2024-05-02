package ru.itmo.programming.client.commands;

import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.WrongArgumentException;

/**
 * @author Nikita Vasilev
 */
public class ExecuteScript extends Command {
    private final Console console;
    public ExecuteScript(Console console) {
        super("execute_script", "считать и исполнить скрипт из указанного файла");
        this.console = console;
    }

    @Override
    public boolean validateArgs(String[] args) {
        return args.length == 1;
    }

    @Override
    public void execute(String[] args) {
        if (!validateArgs(args)) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо указать путь к файлу для выполнения скрипта.");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
            }
        }
    }
}
