package ru.itmo.programming.commands;

import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class Info extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public Info(Console console, CollectionManager collectionManager) {
        super("info", "вывести в стандартный поток вывода информацию о коллекции");
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
            console.println("Информация о текущей коллекции:");
            console.println("Тип: " + collectionManager.getCollectionType());
            if (collectionManager.getElementsType() != null) {
                console.println("Класс коллекции: " + collectionManager.getElementsType());
                console.println("Время инициализации: " + collectionManager.getInitializationDate());
            }
            console.println("Количество элементов: " + collectionManager.getCollectionSize());
        }
    }
}
