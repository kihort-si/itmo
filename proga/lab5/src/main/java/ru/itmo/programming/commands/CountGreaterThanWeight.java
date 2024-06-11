package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class CountGreaterThanWeight extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public CountGreaterThanWeight(Console console, CollectionManager collectionManager) {
        super("count_greater_than_weight", "вывести количество элементов, значение поля weight которых больше заданного");
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
            console.printError("Для выполнения команды " + getName() + " введите вес для сравнения");
        } else {
            try {
                double weight = Double.parseDouble(args[0]);
                int count = 0;
                for (Person person : collectionManager.getCollection()) {
                    if (person.getWeight() > weight) {
                        count++;
                    }
                }
                console.println("Количество элементов с весом больше " + weight + ": " + count);
            } catch (NumberFormatException e) {
                console.println("Ошибка: Некорректный формат веса.");
            }
        }
    }
}
