package ru.itmo.common.utils;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

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

    /**
     * @return a boolean value whether something is entered on the next line
     * @throws IOException if an error occurred while reading through InputStreamReader
     */
    boolean hasNewln() throws IOException;

    /**
     * @return the read string that is entered into the console
     * @throws NoSuchElementException if no scanner is defined that reads the next line
     * @throws IllegalStateException  if Java can't read the next line at the moment
     */
    String readln() throws NoSuchElementException, IllegalStateException;

    /**
     * @return encrypted password
     * @throws UserInterruptException if an error occurred during the building of the password string
     * @throws EndOfFileException     if an error occurs while reading from the terminal
     * @throws IOException            if an error occurred while reading through InputStreamReader
     */
    String passwordReader() throws UserInterruptException, EndOfFileException, IOException;
}
