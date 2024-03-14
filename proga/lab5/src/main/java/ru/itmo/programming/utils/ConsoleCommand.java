package ru.itmo.programming.utils;

/**
 * @author Nikita Vasilev
 */
public interface ConsoleCommand {
    /**
     * @param object object to be output as a string to the console
     */
    void println(Object object);

    /**
     * @param object object to be output as an error to the console
     */
    void printError(Object object);
}
