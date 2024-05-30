package ru.itmo.client.commands;

import ru.itmo.common.exceptions.WrongArgumentException;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class ExecuteScript extends Command {
    private final Console console;
    public ExecuteScript(Console console) {
        super(Commands.EXECUTE_SCRIPT.getName(), Commands.EXECUTE_SCRIPT.getDescription());
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
