package ru.itmo.client.builders;

import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Location;
import ru.itmo.common.utils.Console;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * @author Nikita Vasilev
 */
public class LocationBuilder extends Builder<Location> {
    private Console console;
    private JTextField[][] textFields;
    private JLabel[][] textAreas;
    private ResourceBundle resourceBundle;
    private boolean isGuiMode;

    public LocationBuilder(Console console) {
        this.console = console;
        this.isGuiMode = false;
    }

    public LocationBuilder(JTextField[][] textFields, JLabel[][] textAreas, ResourceBundle resourceBundle) {
        this.textFields = textFields;
        this.textAreas = textAreas;
        this.resourceBundle = resourceBundle;
        this.isGuiMode = true;
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
        if (isGuiMode) {
            String input = textFields[2][1].getText().trim();
            if (input.isEmpty()) {
                textAreas[2][1].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateX(input, textAreas[2][1], resourceBundle);
        } else {
            return consoleX();
        }
    }

    private Float validateX(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Float x = Float.parseFloat(input);
            textAreas.setText("");
            return x;
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
        if (isGuiMode) {
            String input = textFields[3][1].getText().trim();
            if (input.isEmpty()) {
                textAreas[3][1].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateY(input, textAreas[3][1], resourceBundle);
        } else {
            return consoleY();
        }
    }

    private Long validateY(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Long y = Long.parseLong(input);
            textAreas.setText("");
            return y;
        } catch (NumberFormatException e) {
            textAreas.setText(resourceBundle.getString("coordinateError"));
            return null;
        }
    }

    private Long consoleY() {
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
        if (isGuiMode) {
            String input = textFields[3][1].getText().trim();
            if (input.isEmpty()) {
                textAreas[4][1].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateZ(input, textAreas[4][1], resourceBundle);
        } else {
            return consoleZ();
        }
    }

    private Integer validateZ(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Integer y = Integer.parseInt(input);
            textAreas.setText("");
            return y;
        } catch (NumberFormatException e) {
            textAreas.setText(resourceBundle.getString("coordinateError"));
            return null;
        }
    }

    private Integer consoleZ() {
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите координату местоположения Z: ");
                }
                String input = Input.getUserScanner().nextLine().trim();
                int z = Integer.parseInt(input);
                return z;
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
