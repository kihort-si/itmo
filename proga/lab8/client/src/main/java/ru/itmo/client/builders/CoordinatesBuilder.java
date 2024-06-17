package ru.itmo.client.builders;


import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Coordinates;
import ru.itmo.common.utils.Console;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * @author Nikita Vsilev
 */
public class CoordinatesBuilder extends Builder<Coordinates> {
    private Console console;
    private JTextField[][] textFields;
    private JLabel[][] textAreas;
    private ResourceBundle resourceBundle;
    private boolean isGuiMode;

    public CoordinatesBuilder(Console console) {
        this.console = console;
        this.isGuiMode = false;
    }

    public CoordinatesBuilder(JTextField[][] textFields, JLabel[][] textAreas, ResourceBundle resourceBundle) {
        this.textFields = textFields;
        this.textAreas = textAreas;
        this.resourceBundle = resourceBundle;
        this.isGuiMode = true;
    }

    /**
     *
     * @return entered coordinates for the collection element
     */
    @Override
    public Coordinates build() {
        Coordinates coordinates = new Coordinates(X(), Y());
        return coordinates;
    }

    /**
     *
     * @return entered X coordinate for the collection element
     */
    private Float X() {
        if (isGuiMode) {
            String input = textFields[1][0].getText().trim();
            if (input.isEmpty()) {
                textAreas[1][0].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateX(input, textAreas[1][0], resourceBundle);
        } else {
            return consoleX();
        }
    }

    private Float validateX(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Float coordinate = Float.parseFloat(input.trim());
            textAreas.setText("");
            return coordinate;
        } catch (NumberFormatException e) {
            textAreas.setText(resourceBundle.getString("coordinateError"));
            return null;
        }
    }

    private Float consoleX() {
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
     *
     * @return entered Y coordinate for the collection element
     */
    private Float Y() {
        if (isGuiMode) {
            String input = textFields[2][0].getText().trim();
            if (input.isEmpty()) {
                textAreas[2][0].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateY(input, textAreas[2][0], resourceBundle);
        } else {
            return consoleY();
        }
    }

    private Float validateY(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Float coordinate = Float.parseFloat(input.trim());
            textAreas.setText("");
            return coordinate;
        } catch (NumberFormatException e) {
            textAreas.setText(resourceBundle.getString("coordinateError"));
            return null;
        }
    }

    private Float consoleY() {
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
