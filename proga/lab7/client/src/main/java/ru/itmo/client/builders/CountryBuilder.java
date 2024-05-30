package ru.itmo.client.builders;

import ru.itmo.client.utils.Input;
import ru.itmo.common.collection.Country;
import ru.itmo.common.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class CountryBuilder extends Builder<Country> {
    private final Console console;
    public CountryBuilder(Console console) {
        this.console = console;
    }

    /**
     * @return entered country for the collection element
     */
    @Override
    public Country build() {
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
