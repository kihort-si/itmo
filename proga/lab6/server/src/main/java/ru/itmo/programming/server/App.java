package ru.itmo.programming.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.server.commands.*;
import ru.itmo.programming.server.managers.CommandManager;
import ru.itmo.programming.server.managers.FileManager;
import ru.itmo.programming.server.network.Configuration;
import ru.itmo.programming.server.network.ServerManager;
import ru.itmo.programming.server.vaults.CollectionVault;

import java.io.IOException;

public class App {
    public static final Logger logger = LoggerFactory.getLogger("ServerLogger");
    public static void main(String[] args) {
        String host = System.getenv("HOST");
        String strPort = System.getenv("PORT");

        if (host == null || strPort == null) {
            logger.error("Неверные переменные окружения для хоста и порта");
            return;
        }

        int port = Integer.parseInt(strPort);
        Configuration.setHost(host);
        Configuration.setPort(port);

        String filePath = System.getenv("FILE_PATH");
        if (filePath == null) {
            logger.error("Укажите переменную окружения FILE_PATH");
            return;
        }

        Console console = new Console();

        CollectionVault collectionVault = new CollectionVault();
        FileManager fileManager = new FileManager(filePath, collectionVault);

        fileManager.readCollection();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            fileManager.writeCollection(collectionVault.getCollection());
        }));

        CommandManager commandManager = new CommandManager();
        commandManager.createCommand(new Add(collectionVault));
        commandManager.createCommand(new AddIfMax(collectionVault));
        commandManager.createCommand(new AddIfMin(collectionVault));
        commandManager.createCommand(new Clear(collectionVault));
        commandManager.createCommand(new CountGreaterThanWeight(collectionVault));
        commandManager.createCommand(new FilterLessThanHeight(collectionVault));
        commandManager.createCommand(new Info(collectionVault));
        commandManager.createCommand(new MaxByLocation(collectionVault));
        commandManager.createCommand(new RemoveById(collectionVault));
        commandManager.createCommand(new RemoveLower(collectionVault));
        commandManager.createCommand(new Show(collectionVault));
        commandManager.createCommand(new UpdateId(collectionVault));

        ServerManager serverManager = new ServerManager(commandManager, fileManager, collectionVault, console);

        try {
            serverManager.start();
            logger.info("Сервер запущен!");
        } catch (IOException e) {
            logger.error(e.toString());
            logger.error("Не удалось запустить сервер.");
            return;
        }

        serverManager.setAfterHook(() -> {
            fileManager.writeCollection(collectionVault.getCollection());
        });
        serverManager.run();
    }
}
