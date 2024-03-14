package ru.itmo.programming.commands;

import ru.itmo.programming.exceptions.WrongArgumentException;
import ru.itmo.programming.managers.CommandManager;
import ru.itmo.programming.managers.ScriptManager;
import ru.itmo.programming.utils.Console;

import java.io.*;

/**
 * @author Nikita Vasilev
 */
public class ExecuteScript extends Command {
    private final Console console;
    private final CommandManager commandManager;
    private final ScriptManager scriptManager;
    public ExecuteScript(Console console, CommandManager commandManager, ScriptManager scriptManager) {
        super("execute_script", "считать и исполнить скрипт из указанного файла");
        this.console = console;
        this.commandManager = commandManager;
        this.scriptManager = scriptManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            try {
                throw new WrongArgumentException("Ошибка: " + getName() + " - Необходимо указать путь к файлу для выполнения скрипта.");
            } catch (WrongArgumentException e) {
                console.printError(e.getMessage());
                return;
            }
        }

        String fileName = args[0];

        File file = new File(fileName);
        if (!file.isAbsolute()) {
            try {
                fileName = file.getCanonicalPath();
            } catch (IOException e) {
                console.println("Ошибка: Не удалось получить абсолютный путь к файлу.");
                return;
            }
        }

        if (scriptManager.isScriptInStack(fileName)) {
            console.println("Ошибка: Обнаружено зацикливание. Файл скрипта \"" + fileName + "\" уже исполнялся.");
            return;
        }

        scriptManager.addScript(fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] commandAndArgs = line.trim().split("\\s+", 2);
                String commandName = commandAndArgs[0];
                String[] commandArgs = commandAndArgs.length > 1 ? commandAndArgs[1].split("\\s+") : new String[0];

                Command command = commandManager.getCommand(commandName);
                if (command != null) {
                    command.execute(commandArgs);
                } else {
                    console.println("Ошибка: Команда \"" + commandName + "\" не найдена.");
                }
            }
        } catch (FileNotFoundException e) {
            console.println("Ошибка: Файл скрипта не найден.");
        } catch (IOException e) {
            console.println("Ошибка при чтении файла скрипта: " + e.getMessage());
        } finally {
            scriptManager.removeScript();
        }
    }
}
