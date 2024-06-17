package ru.itmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.common.utils.Console;
import ru.itmo.server.commands.*;
import ru.itmo.server.database.ConnectionManager;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.managers.CommandManager;
import ru.itmo.server.network.Configuration;
import ru.itmo.server.network.ServerManager;
import ru.itmo.server.vaults.CollectionVault;

import java.io.IOException;
import java.sql.SQLException;

public class App {
    public static final Logger logger = LoggerFactory.getLogger("ServerLogger");

    public static void main(String[] args) {
        Console console = new Console();

        String host = "127.0.0.1";
        String strPort = "5050";

        if (host == null || strPort == null) {
            console.printError("Неверные переменные окружения для хоста и порта");
            logger.error("Подключение не удалось: введены неверные переменные окружения для хоста и порта");
            return;
        }

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String login = "postgres";
        String password = "postgres";

        if (url == null || login == null || password == null) {
            console.printError("Неверные переменные окружения для подключения к базе данных");
            logger.error("Подключение не удалось: введены неверные переменные окружения для подключения к базе данных");
            return;
        }

        int port = Integer.parseInt(strPort);
        Configuration.setHost(host);
        Configuration.setPort(port);
        Configuration.setUrl(url);
        Configuration.setLogin(login);
        Configuration.setPassword(password);

        ConnectionManager connectionManager = new ConnectionManager(Configuration.getUrl(), Configuration.getLogin(), Configuration.getPassword());
        DatabaseManger databaseManger = new DatabaseManger(connectionManager);
        UserManager userManager = new UserManager(connectionManager);

        try {
            CollectionVault collectionVault = new CollectionVault(databaseManger);
            CommandManager commandManager = new CommandManager();
            commandManager.createCommand(new Add(collectionVault, databaseManger, userManager));
            commandManager.createCommand(new AddIfMax(collectionVault, databaseManger, userManager));
            commandManager.createCommand(new AddIfMin(collectionVault, databaseManger, userManager));
            commandManager.createCommand(new Authorization(userManager));
            commandManager.createCommand(new Clear(collectionVault, databaseManger, userManager));
            commandManager.createCommand(new CountGreaterThanWeight(collectionVault, userManager));
            commandManager.createCommand(new FilterLessThanHeight(collectionVault, userManager));
            commandManager.createCommand(new Info(collectionVault, userManager));
            commandManager.createCommand(new MaxByLocation(collectionVault, userManager));
            commandManager.createCommand(new Register(userManager));
            commandManager.createCommand(new RemoveById(collectionVault, databaseManger, userManager));
            commandManager.createCommand(new RemoveLower(collectionVault, databaseManger, userManager));
            commandManager.createCommand(new Show(collectionVault, userManager));
            commandManager.createCommand(new UpdateId(collectionVault, databaseManger, userManager));

            ServerManager serverManager = new ServerManager(commandManager, console);

            serverManager.start();
            logger.info("Сервер запущен!");

            serverManager.run();
        } catch (SQLException e) {
            logger.error(e.toString());
            logger.error("Не удалось запустить базу данных.");
        } catch (IOException e) {
            logger.error(e.toString());
            logger.error("Не удалось запустить сервер.");
        }
    }
}
