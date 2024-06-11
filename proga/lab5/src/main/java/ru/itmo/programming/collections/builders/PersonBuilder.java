package ru.itmo.programming.collections.builders;

import ru.itmo.programming.collections.*;
import ru.itmo.programming.utils.Console;
import ru.itmo.programming.utils.Input;

import java.time.ZonedDateTime;
import java.util.NoSuchElementException;

/**
 * @author Nikita Vasilev
 */
public class PersonBuilder extends CollectionBuilder<Person>{
    private final Console console;
    private long id;


    public PersonBuilder(long nextId, Console console) {
        this.id = nextId;
        this.console = console;
    }

    @Override
    public Person build() {
        Person person = new Person(
                id,
                enterName(),
                enterCoordinates(),
                ZonedDateTime.now(),
                enterHeight(),
                enterWeight(),
                enterEyeColor(),
                enterNationality(),
                enterLocation());
        if (!person.verificate()) throw new IllegalAccessError();
        return person;
    }

    /**
     * @return entered name for the collection element
     */
    private String enterName() {
        String name;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите имя: ");
                }
                name = Input.getUserScanner().nextLine().trim();
                if (name.isEmpty()) throw new NullPointerException();
                break;
            } catch (NoSuchElementException e) {
                console.printError("Имя не распознано");
            } catch (NullPointerException e) {
                console.printError("Имя не может быть null");
            } catch (IllegalStateException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
        return name;
    }

    /**
     * @return entered coordinates for the collection element
     */
    private Coordinates enterCoordinates() {
        return new CoordinatesBuilder(console).build();
    }

    /**
     * @return entered height for the collection element
     */
    private Double enterHeight() {
        Double height;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите рост: ");
                }
                String input = Input.getUserScanner().nextLine().trim();
                height = Double.parseDouble(input);
                if (height <= 0) {
                    console.printError("Рост не может быть отрицательным.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                console.printError("Некорректный формат роста. Введите число.");
            } catch (NoSuchElementException e) {
                console.printError("Рост не распознан");
            } catch (IllegalStateException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
        return height;
    }

    /**
     * @return entered weight for the collection element
     */
    private double enterWeight() {
        double weight;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите вес: ");
                }
                String input = Input.getUserScanner().nextLine();
                if (input.isEmpty()) {
                    console.printError("Вес не может быть пустым");
                    continue;
                }
                weight = Double.parseDouble(input);
                if (weight <= 0) {
                    console.printError("Вес не может быть отрицательным.");
                    continue;
                }
                break;
            } catch (NoSuchElementException e) {
                console.printError("Рост не распознан");
            } catch (NullPointerException e) {
                console.printError("Рост не может быть null");
            } catch (IllegalArgumentException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
        return weight;
    }

    /**
     * @return entered eye color for the collection element
     */
    private Color enterEyeColor() {
        return new ColorBuilder(console).build();
    }

    /**
     * @return entered nationality for the collection element
     */
    private Country enterNationality() {
        return new CountryBuilder(console).build();
    }

    /**
     * @return entered location coordinates for the collection element
     */
    private Location enterLocation() {
        return new LocationBuilder(console).build();
    }
}
