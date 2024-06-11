package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.collections.builders.PersonBuilder;
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
    public boolean validate(String[] args) {
        return args.length == 1;
    }

    @Override
    public void execute(String[] args) {
        if (!validate(args)) {
            console.printError("Для выполнения команды " + getName() + " введите ID");
        } else {
            long id;
            try {
                id = Long.parseLong(args[0]);
                if (collectionManager.getById(id) != null) {
                    Person updatedPerson = (new PersonBuilder(id, console)).build();
                    collectionManager.getById(id).update(updatedPerson);
                    console.println("Информация о человеке успешно обновлена");
                } else {
                    console.println("Человека с id = " + id + " не существует");
                }
            } catch (NumberFormatException e) {
                console.printError("Ошибка: Некорректный формат id");
            }
        }
    }
}
