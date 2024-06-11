package ru.itmo.programming.commands;

import ru.itmo.programming.collections.builders.PersonBuilder;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

import java.util.Arrays;

/**
 * @author Nikita Vasilev
 */
public class Add extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public Add(Console console, CollectionManager collectionManager) {
        super("add", "добавить новый элемент в коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
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
            long nextId = collectionManager.freeIds();
            collectionManager.addElementToCollection(new PersonBuilder(nextId, console).build());
            console.println("Человек успешно создан");
        }
    }
}
