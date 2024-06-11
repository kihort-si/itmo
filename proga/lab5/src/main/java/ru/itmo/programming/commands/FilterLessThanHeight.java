package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Nikita Vasilev
 */
public class FilterLessThanHeight extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public FilterLessThanHeight(Console console, CollectionManager collectionManager) {
        super("filter_less_than_height", "вывести элементы, значение поля height которых меньше заданного");
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
            console.printError("Для выполнения команды " + getName() + " введите рост для сравнения");
        } else {
            try {
                double height = Double.parseDouble(args[0]);
                List<Person> sortedList = new ArrayList<>(collectionManager.getCollection());
                Collections.sort(sortedList);
                for (Person person : sortedList) {
                    if (person.getHeight() < height) {
                        console.println(person.toString());
                    }
                }
            } catch (NumberFormatException e) {
                console.println("Ошибка: Некорректный формат роста.");
            }
        }
    }
}