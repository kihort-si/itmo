package ru.itmo.programming.commands;

import ru.itmo.programming.exceptions.WrongArgumentException;
import ru.itmo.programming.managers.CommandManager;
import ru.itmo.programming.utils.Console;

import java.io.*;

/**
 * @author Nikita Vasilev
 */
public class ExecuteScript extends Command {
    private final Console console;
    private final CommandManager commandManager;
    public ExecuteScript(Console console, CommandManager commandManager) {
        super("execute_script", "считать и исполнить скрипт из указанного файла");
        this.console = console;
        this.commandManager = commandManager;
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
                    console.printError("Команда \"" + commandName + "\" не найдена.");
                }
            }
        } catch (FileNotFoundException e) {
            console.printError("Файл скрипта не найден.");
        } catch (IOException e) {
            console.printError("Не удалось прочитать файл: " + e.getMessage());
        }
    }
}
