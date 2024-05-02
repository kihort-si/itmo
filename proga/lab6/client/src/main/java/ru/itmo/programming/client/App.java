package ru.itmo.programming.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.client.network.Configuration;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.client.utils.Runner;
import ru.itmo.programming.client.utils.ScriptManager;

public class App {
    public static final Logger logger = LoggerFactory.getLogger("ClientLogger");

    public static void main(String[] args) {
        Console console = new Console();

        String host = System.getenv("HOST");
        String strPort = System.getenv("PORT");

        if (host == null || strPort == null) {
            logger.error("Неверные переменные окружения для хоста и порта");
            return;
        }

        int port = Integer.parseInt(strPort);
        Configuration.setHost(host);
        Configuration.setPort(port);

        ScriptManager scriptManager = new ScriptManager();
        ClientManager clientManager = new ClientManager();

        new Runner(console, clientManager, scriptManager).interactiveMode();
    }
}
