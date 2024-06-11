package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.collections.builders.PersonBuilder;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class AddIfMin extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public AddIfMin(Console console, CollectionManager collectionManager) {
        super("add_if_min", "добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции");
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
            Person person = new PersonBuilder(nextId, console).build();

            double minHeight = collectionManager.getMinHeight();

            if (person.getHeight() == minHeight) {
                collectionManager.addElementToCollection(person);
                console.println("Человек успешно добавлен");
            } else {
                console.println("Человек не добавлен, так как его рост больше минимального");
            }
        }
    }
}