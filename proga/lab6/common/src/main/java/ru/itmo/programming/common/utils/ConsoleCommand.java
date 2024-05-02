package ru.itmo.programming.common.utils;

import java.io.IOException;
import java.util.NoSuchElementException;

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

    boolean hasNewln() throws IOException;

    String readln() throws NoSuchElementException, IllegalStateException;
}
