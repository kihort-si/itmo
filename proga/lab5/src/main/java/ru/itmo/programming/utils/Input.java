package ru.itmo.programming.utils;

import java.util.Scanner;

/**
 * @author Nikita Vasilev
 */
public class Input {
    private static Scanner scanner;
    private static boolean fileMode = false;

    /**
     * @return current scanner type (from the console or from a file)
     */
    public static Scanner getUserScanner() {
        return scanner;
    }

    /**
     * @param userScanner sets the current scanner type (from the console or from a file)
     */
    public static void setUserScanner(Scanner userScanner) {
        Input.scanner = userScanner;
    }

    /**
     * @return boolean value, whether input from the file is currently working
     */
    public static boolean isFileMode() {
        return fileMode;
    }

    /**
     * @param fileMode sets input from a file or console
     */
    public static void setFileMode(boolean fileMode) {
        Input.fileMode = fileMode;
    }
}
