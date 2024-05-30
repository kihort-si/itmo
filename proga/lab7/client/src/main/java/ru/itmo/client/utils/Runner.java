package ru.itmo.client.utils;

import ru.itmo.client.commands.*;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.Console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Nikita Vasilev
 */
public class Runner {

    private final Console console;
    private final Map<String, Command> commands;
    private final ScriptManager scriptManager;

    public Runner(Console console, ClientManager clientManager, ScriptManager scriptManager) {
        this.console = console;
        this.scriptManager = scriptManager;
        this.commands = new HashMap<>() {{
            put(Commands.ADD.getName(), new Add(console, clientManager));
            put(Commands.ADD_IF_MAX.getName(), new AddIfMax(console, clientManager));
            put(Commands.ADD_IF_MIN.getName(), new AddIfMin(console, clientManager));
            put(Commands.AUTHORIZATION.getName(), new Authorization(console, clientManager));
            put(Commands.CLEAR.getName(), new Clear(console, clientManager));
            put(Commands.COUNT_GREATER_THAN_WEIGHT.getName(), new CountGreaterThanWeight(console, clientManager));
            put(Commands.EXECUTE_SCRIPT.getName(), new ExecuteScript(console));
            put(Commands.EXIT.getName(), new Exit(console));
            put(Commands.FILTER_LESS_THAN_HEIGHT.getName(), new FilterLessThanHeight(console, clientManager));
            put(Commands.HELP.getName(), new Help(console));
            put(Commands.INFO.getName(), new Info(console, clientManager));
            put(Commands.MAX_BY_LOCATION.getName(), new MaxByLocation(console, clientManager));
            put(Commands.REGISTER.getName(), new Register(console, clientManager));
            put(Commands.REMOVE_BY_ID.getName(), new RemoveById(console, clientManager));
            put(Commands.REMOVE_LOWER.getName(), new RemoveLower(console, clientManager));
            put(Commands.SHOW.getName(), new Show(console, clientManager));
            put(Commands.UPDATE_ID.getName(), new UpdateId(console, clientManager));
        }};
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
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

    public void executeCommand(String[] commandAndArgs) {
        String commandName = commandAndArgs[0];
        String[] commandArgs = commandAndArgs.length > 1 ? Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length) : new String[0];

        Command command = getCommand(commandName);
        if (command != null) {
            command.execute(commandArgs);
        } else {
            console.printError("Неизвестная команда: " + commandName);
            console.println("");
        }
    }
}
