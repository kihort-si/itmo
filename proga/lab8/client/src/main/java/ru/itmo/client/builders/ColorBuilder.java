package ru.itmo.client.builders;


import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Color;
import ru.itmo.common.utils.Console;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;


/**
 * @author Nikita Vasilev
 */
public class ColorBuilder extends Builder<Color> {
    private Console console;
    private JTextField[][] textFields;
    private JLabel[][] textAreas;
    private ResourceBundle resourceBundle;
    private boolean isGuiMode;

    public ColorBuilder(Console console) {
        this.console = console;
        this.isGuiMode = false;
    }

    public ColorBuilder(JTextField[][] textFields, JLabel[][] textAreas, ResourceBundle resourceBundle) {
        this.textFields = textFields;
        this.textAreas = textAreas;
        this.resourceBundle = resourceBundle;
        this.isGuiMode = true;
    }

    /**
     * @return entered eye color for the collection element
     */
    @Override
    public Color build() {
        if (isGuiMode) {
            String input = textFields[0][1].getText().trim().toUpperCase();
            if (input.isEmpty()) {
                textAreas[0][1].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateColor(input, textAreas[0][1], resourceBundle);
        } else {
            return consoleColor();
        }
    }

    private Color validateColor(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Color color = Color.valueOf(input);
            textAreas.setText("");
            return color;
        } catch (NoSuchElementException e) {
            textAreas.setText(resourceBundle.getString("eyeColorEnumError"));
            return null;
        }
    }

    private Color consoleColor() {
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
