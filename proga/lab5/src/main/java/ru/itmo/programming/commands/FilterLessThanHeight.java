package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Person;
import ru.itmo.programming.exceptions.WrongArgumentException;
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
    public void execute(String[] args) {
        if (args.length < 1) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо указать рост для сравнения.");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
                return;
            }
        }

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