package ru.itmo.client.builders;

import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Country;
import ru.itmo.common.utils.Console;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * @author Nikita Vasilev
 */
public class CountryBuilder extends Builder<Country> {
    private Console console;
    private JTextField[][] textFields;
    private JLabel[][] textAreas;
    private ResourceBundle resourceBundle;
    private boolean isGuiMode;
    public CountryBuilder(Console console) {
        this.console = console;
        this.isGuiMode = false;
    }

    public CountryBuilder(JTextField[][] textFields, JLabel[][] textAreas, ResourceBundle resourceBundle) {
        this.textFields = textFields;
        this.textAreas = textAreas;
        this.resourceBundle = resourceBundle;
        this.isGuiMode = true;
    }

    /**
     * @return entered country for the collection element
     */
    @Override
    public Country build() {
        if (isGuiMode) {
            String input = textFields[1][1].getText().trim().toUpperCase();
            if (input.isEmpty()) {
                textAreas[1][1].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateCountry(input, textAreas[1][1], resourceBundle);
        } else {
            return consoleCountry();
        }
    }

    private Country validateCountry(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Country country = Country.valueOf(input);
            textAreas.setText("");
            return country;
        } catch (NoSuchElementException e) {
            textAreas.setText(resourceBundle.getString("eyeColorEnumError"));
            return null;
        }
    }

    private Country consoleCountry() {
        boolean fileMode = Input.isFileMode();
        Country country;
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Выберете национальность из списка: " + "\u001B[3;93m" + Country.getAsString() + "\u001B[0;0m");
                }
                String input = Input.getUserScanner().nextLine().trim();
                if (input.isEmpty()) {
                    console.printError("Национальность не может быть пустым");
                    continue;
                }
                try {
                    country = Country.valueOf(input.toUpperCase());
                    break;
                } catch (IllegalArgumentException e) {
                    console.printError("Такой национальности нет в списке");
                }
            } catch (IllegalStateException e) {
                console.printError("Произошла непредвиденная ошибка");
            }
        }
        return country;
    }
}
