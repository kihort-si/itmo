package ru.itmo.programming.utils;

import ru.itmo.programming.commands.Command;
import ru.itmo.programming.managers.CommandManager;
import ru.itmo.programming.managers.ScriptManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * @author Nikita Vasilev
 */
public class Runner {

    private final Console console;
    private final CommandManager commandManager;
    private final ScriptManager scriptManager;

    public Runner(Console console, CommandManager commandManager, ScriptManager scriptManager) {
        this.console = console;
        this.commandManager = commandManager;
        this.scriptManager = scriptManager;
    }

    /**
     * Works with console input.
     */
    public void interactiveMode() {
        Scanner scanner = new Scanner(System.in);
        Input.setUserScanner(scanner);
        String input;

        console.println("Интерактивный режим. Введите команду: ");

        while (true) {
            input = scanner.nextLine().trim();
            if (input.equals("exit")) {
                break;
            } else if (input.startsWith("execute_script")) {
                String fileName = input.split(" ", 2)[1];
                File file = new File(fileName);
                try {
                    fileName = file.getCanonicalPath();
                } catch (IOException e) {
                    console.printError("Не удалось получить абсолютный путь к файлу.");
                    return;
                }
                scriptManager.addScript(fileName);
                fileMode(fileName);
                scriptManager.removeScript();
            } else {
                executeCommand(input.split(" "));
            }
        }
    }

    /**
     * Works when executing commands from the script file.
     * @param fileName name of the script file from which you want to read data
     */
    public void fileMode(String fileName) {
        boolean previousFileMode = Input.isFileMode();
        try (Scanner fileScanner = new Scanner(new File(fileName))) {
            Scanner previousScanner = Input.getUserScanner();
            Input.setUserScanner(fileScanner);
            Input.setFileMode(true);

            while (fileScanner.hasNextLine()) {
                String commandLine = fileScanner.nextLine().trim();
                String[] commandAndArgs = commandLine.split("\\s+", 2);
                String commandName = commandAndArgs[0];
                String[] commandArgs = commandAndArgs.length > 1 ? commandAndArgs[1].split("\\s+") : new String[0];
                if (commandName.equals("execute_script")) {
                    String scriptPath = commandArgs[0];
                    File file = new File(scriptPath);
                    try {
                        scriptPath = file.getCanonicalPath();
                    } catch (IOException e) {
                        console.printError("Не удалось получить абсолютный путь к файлу.");
                        return;
                    }
                    if (!scriptManager.isScriptInStack(scriptPath)) {
                        scriptManager.addScript(scriptPath);
                        fileMode(scriptPath);
                        scriptManager.removeScript();
                    } else {
                        console.printError("Обнаружено зацикливание. Файл скрипта \"" + scriptPath + "\" уже исполняется. Выполнение будет пропущено.");
                    }
                } else {
                    executeCommand(commandLine.split(" "));
                }
            }
            Input.setUserScanner(previousScanner);
        } catch (FileNotFoundException e) {
            console.printError("Файл не найден: " + fileName);
        } finally {
            Input.setFileMode(previousFileMode);
        }
    }


    /**
     * @param commandAndArgs a string from a console or file to be used as a command and arguments
     */
    public void executeCommand(String[] commandAndArgs) {
        String commandName = commandAndArgs[0];
        String[] commandArgs = commandAndArgs.length > 1 ? Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length) : new String[0];

        Command command = commandManager.getCommand(commandName);
        if (command != null) {
            command.execute(commandArgs);
        } else {
            console.printError("Неизвестная команда: " + commandName);
            console.println("");
        }
    }
}
