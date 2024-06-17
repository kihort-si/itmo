package ru.itmo.programming.common.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author Nikita Vasilev
 */
public class Console implements ConsoleCommand {
    private Scanner fileScanner = null;
    private final InputStreamReader reader = new InputStreamReader(System.in);
    private final Scanner defScanner = new Scanner(reader);

    @Override
    public void println(Object object) {
        System.out.println(object);
    }

    @Override
    public void printError(Object object) {
        System.err.println("\u001B[0;31m" + "Ошибка: " + object + "\u001B[0;0m");
    }

    @Override
    public boolean hasNewln() throws IOException {
        return reader.ready();
    }

    @Override
    public String readln() throws NoSuchElementException, IllegalStateException {
        return (fileScanner != null ? fileScanner : defScanner).nextLine();
    }
}
