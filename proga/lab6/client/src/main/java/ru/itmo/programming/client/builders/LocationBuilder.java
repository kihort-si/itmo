package ru.itmo.programming.client.builders;

import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.client.utils.Input;
import ru.itmo.programming.common.collection.Location;

import java.util.NoSuchElementException;

/**
 * @author Nikita Vasilev
 */
public class LocationBuilder extends CollectionBuilder<Location> {
    private final Console console;

    public LocationBuilder(Console console) {
        this.console = console;
    }

    /**
     * @return entered location coordinates for the collection element
     */
    @Override
    public Location build() {
        Location location = new Location(X(), Y(), Z());
        return location;
    }

    /**
     * @return entered X location coordinate for the collection element
     */
    public Float X() {
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите координату местоположения X: ");
                }
                String input = Input.getUserScanner().nextLine().trim();
                Float x = Float.parseFloat(input);
                return x;
            } catch (NumberFormatException e) {
                console.printError("Некорректный формат координаты. Введите число.");
            } catch (NoSuchElementException e) {
                console.printError("Координата не распознана");
            } catch (NullPointerException e) {
                console.printError("Координата не может быть null");
            } catch (IllegalArgumentException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
    }

    /**
     * @return entered Y location coordinate for the collection element
     */
    public Long Y() {
        while (true) {
            boolean fileMode = Input.isFileMode();
            try {
                if (!fileMode) {
                    console.println("Введите координату местоположения Y: ");
                }
                String input = Input.getUserScanner().nextLine().trim();
                Long y = Long.parseLong(input);
                return y;
            } catch (NumberFormatException e) {
                console.printError("Некорректный формат координаты. Введите число.");
            } catch (NoSuchElementException e) {
                console.printError("Координата не распознана");
            } catch (NullPointerException e) {
                console.printError("Координата не может быть null");
            } catch (IllegalArgumentException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
    }

    /**
     * @return entered Z location coordinate for the collection element
     */
    public Integer Z() {
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите координату местоположения Z: ");
                }
                String input = Input.getUserScanner().nextLine().trim();
                int y = Integer.parseInt(input);
                return y;
            } catch (NumberFormatException e) {
                console.printError("Некорректный формат координаты. Введите число.");
            } catch (NoSuchElementException e) {
                console.printError("Координата не распознана");
            } catch (NullPointerException e) {
                console.printError("Координата не может быть null");
            } catch (IllegalArgumentException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
    }
}
