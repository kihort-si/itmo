package ru.itmo.programming.commands;

import ru.itmo.programming.managers.FileManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class Save extends Command {
    private final Console console;
    private final FileManager fileManager;

    public Save(Console console, FileManager fileManager) {
        super("save", "сохранить коллекцию в файл");
        this.console = console;
        this.fileManager = fileManager;
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
            fileManager.writeCollection();
            console.println("Коллекция успешно сохранена");
        }
    }
}
