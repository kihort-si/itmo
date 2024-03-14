package ru.itmo.programming.utils;

/**
 * @author Nikita Vasilev
 */
public class Console implements ConsoleCommand {
    @Override
    public void println(Object object) {
        System.out.println(object);
    }

    @Override
    public void printError(Object object) {
        System.err.println("Ошибка: " + object);
    }

}
