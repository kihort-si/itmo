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
public class Show extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public Show(Console console, CollectionManager collectionManager) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        if (collectionManager.getCollection().isEmpty()) {
            console.println("Коллекция пуста.");
            return;
        }

        List<Person> sortedList = new ArrayList<>(collectionManager.getCollection());
        Collections.sort(sortedList);


        for (Person person : sortedList) {
            console.println(person.toString());
        }
    }
}
