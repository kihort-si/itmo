package ru.itmo.programming.commands;

import ru.itmo.programming.managers.FileManager;

/**
 * @author Nikita Vasilev
 */
public class Save extends Command {
    private final FileManager fileManager;

    public Save(FileManager fileManager) {
        super("save", "сохранить коллекцию в файл");
        this.fileManager = fileManager;
    }

    @Override
    public void execute(String[] args) {
        fileManager.writeCollection();
    }
}
