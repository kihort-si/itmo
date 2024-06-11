package ru.itmo.programming.commands;

import ru.itmo.programming.utils.Console;


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
    public boolean validate(String[] args) {
        return args.length == 1;
    }

    @Override
    public void execute(String[] args) {
        if (!validate(args)) {
            console.printError("Для выполнения команды " + getName() + " введите путь к файлу для выполнения скрипта");
        }
    }
}
