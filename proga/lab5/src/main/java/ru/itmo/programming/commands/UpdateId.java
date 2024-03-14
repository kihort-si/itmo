package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.collections.builders.PersonBuilder;
import ru.itmo.programming.exceptions.WrongArgumentException;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class UpdateId extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public UpdateId(Console console, CollectionManager collectionManager) {
        super("update_id", "обновить значение элемента коллекции, id которого равен заданному");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо ввести id.");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
                return;
            }
        }

        long id = 0;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            console.printError("Ошибка: Некорректный формат id");
        }

        Person existingPerson = collectionManager.getElementById(id);
        if (existingPerson == null) {
            console.println("Человека с id = " + id + " не существует");
        } else {
            collectionManager.removeById(id);
            collectionManager.addElementToCollection(new PersonBuilder(id, console).build());
            console.println("Информация о человеке успешно обновлена");
        }
    }
}
