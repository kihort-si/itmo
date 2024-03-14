package ru.itmo.programming.commands;

import ru.itmo.programming.collections.builders.PersonBuilder;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class AddIfMax extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public AddIfMax(Console console, CollectionManager collectionManager) {
        super("add_if_max", "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        long nextId = collectionManager.freeIds();
        collectionManager.addElementToCollection(new PersonBuilder(nextId, console).build());
        long addedId = collectionManager.lastCreatedPerson();
        double maxHeight = collectionManager.getMaxHeight();

        if (collectionManager.getElementById(addedId).getHeight() == maxHeight) {
            console.println("Человек успешно добавлен");
        } else {
            collectionManager.removeById(addedId);
            console.println("Человек не добавлен, так как его рост меньше максимального");
        }
    }
}
