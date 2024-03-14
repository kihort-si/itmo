package ru.itmo.programming.commands;

import ru.itmo.programming.exceptions.WrongArgumentException;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class RemoveById extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public RemoveById(Console console, CollectionManager collectionManager) {
        super("remove_by_id", "удалить элемент из коллекции по его id");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо указать ID элемента для удаления.");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
                return;
            }
        }

        try {
            int idToRemove = Integer.parseInt(args[0]);
            if (collectionManager.removeById(idToRemove)) {
                console.println("Элемент с ID " + idToRemove + " успешно удален.");
            } else {
                console.println("Элемент с ID " + idToRemove + " не найден.");
            }
        } catch (NumberFormatException e) {
            console.println("Ошибка: Некорректный формат ID. ID должен быть целым числом.");
        }
    }
}
