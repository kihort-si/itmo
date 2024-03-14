package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.exceptions.WrongArgumentException;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nikita Vasilev
 */
public class RemoveLower extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public RemoveLower(Console console, CollectionManager collectionManager) {
        super("remove_lower", "удалить из коллекции все элементы, меньшие, чем заданный");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо рост, людей меньше которого нужно удалить");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
                return;
            }
        }

        Double height_value = Double.valueOf(args[0]);
        Set<Long> lowerElements = new HashSet<>();
        for (Person person : collectionManager.getCollection()) {
            if (person.getHeight() < height_value) {
                lowerElements.add(person.getId());
            }
        }
        if (lowerElements.isEmpty()) {
            console.println("В коллекции нет элементов с ростом меньше указанного");
        } else {
            for (long id : lowerElements) {
                collectionManager.removeById(id);
            }
            console.println("Элементы успешно удалены");
        }
    }
}
