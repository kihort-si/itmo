package ru.itmo.programming.collections.builders;

import ru.itmo.programming.collections.Color;
import ru.itmo.programming.utils.Console;
import ru.itmo.programming.utils.Input;

import java.util.NoSuchElementException;

import static ru.itmo.programming.utils.Input.isFileMode;

/**
 * @author Nikita Vasilev
 */
public class ColorBuilder extends CollectionBuilder<Color>{
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
        boolean fileMode = isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Выберете цвет глаз из списка: " + "\u001B[3;93m" + Color.getAsString() + "\u001B[0;0m");
                }

                input = Input.getUserScanner().nextLine().trim();

                color = Color.valueOf(input.toUpperCase());
                break;
            }catch (NoSuchElementException exception) {
                console.printError("Тип организации не распознан!");
            } catch (IllegalArgumentException exception) {
                console.printError("Типа организации нет в списке!");
            } catch (IllegalStateException exception) {
                console.printError("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return color;
    }
}
