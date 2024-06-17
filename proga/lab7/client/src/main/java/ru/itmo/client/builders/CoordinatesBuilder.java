package ru.itmo.client.builders;

import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Coordinates;
import ru.itmo.common.utils.Console;

import java.util.NoSuchElementException;

/**
 * @author Nikita Vsilev
 */
public class CoordinatesBuilder extends Builder<Coordinates> {
    private final Console console;

    public CoordinatesBuilder(Console console) {
        this.console = console;
    }

    /**
     * @return entered coordinates for the collection element
     */
    @Override
    public Coordinates build() {
        Coordinates coordinates = new Coordinates(X(), Y());
        return coordinates;
    }

    /**
     * @return entered X coordinate for the collection element
     */
    public Float X() {
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите координату X: ");
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
     * @return entered Y coordinate for the collection element
     */
    public Float Y() {
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите координату Y: ");
                }
                String input = Input.getUserScanner().nextLine().trim();
                float y = Float.parseFloat(input);
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
