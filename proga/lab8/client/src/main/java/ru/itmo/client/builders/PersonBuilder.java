package ru.itmo.client.builders;

import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.*;
import ru.itmo.common.utils.Console;

import javax.swing.*;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * @author Nikita Vasilev
 */
public class PersonBuilder extends Builder<Person> {
    private Console console;
    private JTextField[][] textFields;
    private JLabel[][] textAreas;
    private ResourceBundle resourceBundle;
    private boolean isGuiMode;

    public PersonBuilder(Console console) {
        this.console = console;
        this.isGuiMode = false;
    }

    public PersonBuilder(JTextField[][] textFields, JLabel[][] textAreas, ResourceBundle resourceBundle) {
        this.textFields = textFields;
        this.textAreas = textAreas;
        this.resourceBundle = resourceBundle;
        this.isGuiMode = true;
    }

    @Override
    public Person build() {
        Person person = new Person(
                Long.MAX_VALUE,
                enterName(),
                enterCoordinates(),
                ZonedDateTime.now(),
                enterHeight(),
                enterWeight(),
                enterEyeColor(),
                enterNationality(),
                enterLocation(),
                Integer.MAX_VALUE);
        if (!person.verificate()) throw new IllegalAccessError();
        return person;
    }

    /**
     * @return entered name for the collection element
     */
    private String enterName() {
        if (isGuiMode) {
            String name = textFields[0][0].getText().trim();

            return validateName(name, textAreas[0][0], resourceBundle);
        } else {
            return consoleName();
        }
    }

    private String validateName(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        if (input.isEmpty()) {
            textAreas.setText(resourceBundle.getString("nameError"));
            return null;
        } else {
            textAreas.setText("");
            return input;
        }
    }

    private String consoleName() {
        String name;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите имя: ");
                }
                name = Input.getUserScanner().nextLine().trim();
                if (name.equals("") || name.equals(null)) throw new NullPointerException();
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
        if (isGuiMode) {
            return new CoordinatesBuilder(textFields, textAreas, resourceBundle).build();
        } else {
            return new CoordinatesBuilder(console).build();
        }
    }

    /**
     * @return entered height for the collection element
     */
    private Double enterHeight() {
        if (isGuiMode) {
            String input = textFields[3][0].getText().trim();
            if (input.isEmpty()) {
                textAreas[3][0].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateHeight(input, textAreas[3][0], resourceBundle);
        } else {
            return consoleEnterHeight();
        }
    }

    private Double validateHeight(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Double height = Double.parseDouble(input.trim());
            if (height <= 0) {
                textAreas.setText(resourceBundle.getString("heightError"));
                return null;
            }
            textAreas.setText("");
            return height;
        } catch (NumberFormatException e) {
            textAreas.setText(resourceBundle.getString("inputNotNumber"));
            return null;
        }
    }

    private Double consoleEnterHeight() {
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
    private Double enterWeight() {
        if (isGuiMode) {
            String input = textFields[4][0].getText().trim();
            if (input.isEmpty()) {
                textAreas[4][0].setText(resourceBundle.getString("notNull"));
                return null;
            }
            return validateWeight(input, textAreas[4][0], resourceBundle);
        } else {
            return consoleEnterWeight();
        }
    }

    private Double validateWeight(String input, JLabel textAreas, ResourceBundle resourceBundle) {
        try {
            Double height = Double.parseDouble(input.trim());
            if (height <= 0) {
                textAreas.setText(resourceBundle.getString("weightError"));
                return null;
            }
            textAreas.setText("");
            return height;
        } catch (NumberFormatException e) {
            textAreas.setText(resourceBundle.getString("inputNotNumber"));
            return null;
        }
    }

    private double consoleEnterWeight() {
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
        if (isGuiMode) {
            return new ColorBuilder(textFields, textAreas, resourceBundle).build();
        } else {
            return new ColorBuilder(console).build();
        }
    }

    /**
     * @return entered nationality for the collection element
     */
    private Country enterNationality() {
        if (isGuiMode) {
            return new CountryBuilder(textFields, textAreas, resourceBundle).build();
        } else {
            return new CountryBuilder(console).build();
        }
    }

    /**
     * @return entered location coordinates for the collection element
     */
    private Location enterLocation() {
        if (isGuiMode) {
            return new LocationBuilder(textFields, textAreas, resourceBundle).build();
        } else {
            return new LocationBuilder(console).build();
        }
    }
}
