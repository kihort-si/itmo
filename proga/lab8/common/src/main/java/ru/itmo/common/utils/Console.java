package ru.itmo.common.utils;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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

    @Override
    public String passwordReader() {
        LineReader reader = null;
        try {
            Terminal terminal = TerminalBuilder.terminal();
            reader = LineReaderBuilder.builder().terminal(terminal).build();
        } catch (UserInterruptException | EndOfFileException | IOException e) {
            printError(e.getMessage());
        }

        return reader != null ? reader.readLine('*') : null;
    }
}
