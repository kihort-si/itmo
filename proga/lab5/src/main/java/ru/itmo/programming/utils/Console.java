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
        System.err.println("\u001B[0;31m" + "Ошибка: " + object + "\u001B[0;0m");
    }

}
