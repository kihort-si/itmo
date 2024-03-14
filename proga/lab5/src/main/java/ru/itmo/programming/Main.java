package ru.itmo.programming;

import ru.itmo.programming.commands.*;
import ru.itmo.programming.managers.*;
import ru.itmo.programming.utils.Console;
import ru.itmo.programming.utils.Runner;

public class Main {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Программа прервана");
        }));

        CollectionManager collectionManager = new CollectionManager();
        Console console = new Console();
        ScriptManager scriptManager = new ScriptManager();


        String filePath = System.getenv("FILE_PATH");
        if (filePath == null) {
            console.printError("Укажите переменную окружения FILE_PATH");
            return;
        }

        CommandManager commandManager = new CommandManager();
        FileManager fileManager = new FileManager(filePath, collectionManager, console);

        commandManager.createCommand(new Add(console, collectionManager));
        commandManager.createCommand(new AddIfMax(console, collectionManager));
        commandManager.createCommand(new AddIfMin(console, collectionManager));
        commandManager.createCommand(new Clear(console, collectionManager));
        commandManager.createCommand(new CountGreaterThanWeight(console, collectionManager));
        commandManager.createCommand(new ExecuteScript(console, commandManager, scriptManager));
        commandManager.createCommand(new Exit(console));
        commandManager.createCommand(new FilterLessThanHeight(console, collectionManager));
        commandManager.createCommand(new Help(console, commandManager));
        commandManager.createCommand(new Info(console, collectionManager));
        commandManager.createCommand(new MaxByLocation(console, collectionManager));
        commandManager.createCommand(new RemoveById(console, collectionManager));
        commandManager.createCommand(new RemoveLower(console, collectionManager));
        commandManager.createCommand(new Save(fileManager, collectionManager, filePath));
        commandManager.createCommand(new Show(console, collectionManager));
        commandManager.createCommand(new UpdateId(console, collectionManager));

        fileManager.readCollection(collectionManager.getCollection(), filePath);

        new Runner(console, commandManager).interactiveMode();
    }
}
