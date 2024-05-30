package ru.itmo.client.builders;


import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Color;
import ru.itmo.common.utils.Console;

import java.util.NoSuchElementException;


/**
 * @author Nikita Vasilev
 */
public class ColorBuilder extends Builder<Color> {
    private final Console console;

    public ColorBuilder(Console console) {
        this.console = console;
    }

    /**
     * @return entered eye color for the collection element
     */
    @Override
    public Color build() {
        String input;
        Color color;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Выберете цвет глаз из списка: " + "\u001B[3;93m" + Color.getAsString() + "\u001B[0;0m");
                }

                input = Input.getUserScanner().nextLine().trim();

                color = Color.valueOf(input.toUpperCase());
                break;
            } catch (NoSuchElementException exception) {
                console.printError("Цвет глаз не распознан!");
            } catch (IllegalArgumentException exception) {
                console.printError("Такого цвета глаз нет в списке!");
            } catch (IllegalStateException exception) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
        return color;
    }
}
