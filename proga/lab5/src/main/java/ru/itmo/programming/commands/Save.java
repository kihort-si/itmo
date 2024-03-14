package ru.itmo.programming.commands;

import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.managers.FileManager;

/**
 * @author Nikita Vasilev
 */
public class Save extends Command {
    private final FileManager fileManager;
    private final CollectionManager collectionManager;
    private final String filePath;
    public Save(FileManager fileManager, CollectionManager collectionManager, String filePath) {
        super("save", "сохранить коллекцию в файл");
        this.fileManager = fileManager;
        this.collectionManager = collectionManager;
        this.filePath = filePath;
    }

    @Override
    public void execute(String[] args) {
        fileManager.writeCollection(collectionManager.getCollection(), filePath);
    }
}
