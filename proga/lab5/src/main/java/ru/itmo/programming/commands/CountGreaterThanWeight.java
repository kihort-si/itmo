package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.exceptions.WrongArgumentException;
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
    public void execute(String[] args) {
        if (args.length < 1) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо указать вес для сравнения.");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
                return;
            }
        }

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
