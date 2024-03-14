package ru.itmo.programming.commands;

import ru.itmo.programming.collections.Location;
import ru.itmo.programming.collections.Person;
import ru.itmo.programming.exceptions.EmptyCollectionException;
import ru.itmo.programming.managers.CollectionManager;
import ru.itmo.programming.utils.Console;

import java.util.Comparator;
import java.util.Optional;

/**
 * @author Nikita Vasilev
 */
public class MaxByLocation extends Command {
    private final Console console;
    private final CollectionManager collectionManager;
    public MaxByLocation(Console console, CollectionManager collectionManager) {
        super("max_by_location", "вывести любой объект из коллекции, значение поля location которого является максимальным");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            console.println("Ошибка: Неправильное количество аргументов. Использование: max_by_location");
            return;
        }

        try {
            if (collectionManager.getCollection().isEmpty()) throw new EmptyCollectionException("Ошибка: Коллекция пуста.");
            Comparator<Location> locationComparator = Comparator.comparing(Location::getX)
                    .thenComparing(Location::getY)
                    .thenComparing(Location::getZ);
            Optional<Location> maxLocation = collectionManager.getCollection().stream()
                    .map(Person::getLocation)
                    .max(locationComparator);


            if (maxLocation.isPresent()) {
                Optional<Person> personWithMaxLocation = collectionManager.getCollection().stream()
                        .filter(person -> person.getLocation().equals(maxLocation.get()))
                        .findFirst();

                personWithMaxLocation.ifPresentOrElse(
                        person -> console.println("Человек с максимальным значением location: " + person),
                        () -> console.println("Ошибка: Не удалось найти человека с максимальным значением location.")
                );
            } else {
                console.println("Ошибка: Не удалось найти человека с максимальным значением location.");
            }
        } catch (EmptyCollectionException e) {
            e.getMessage();
        }
    }
}
