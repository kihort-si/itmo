package ru.itmo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.client.gui.GuiManager;
import ru.itmo.client.network.ClientManager;
import ru.itmo.client.network.Configuration;
import ru.itmo.client.utils.Runner;
import ru.itmo.client.utils.ScriptManager;
import ru.itmo.common.utils.Console;

public class App {
    public static final Logger logger = LoggerFactory.getLogger("ClientLogger");

    public static void main(String[] args) {

        Console console = new Console();

        String host = "127.0.0.1";
        String strPort = "5050";

                if (host == null || strPort == null) {
                    console.printError("Неверные переменные окружения для хоста и порта");
                    logger.error("Подключение не удалось: введены неверные переменные окружения для хоста и порта");
                    return;
                }

                int port = Integer.parseInt(strPort);
                Configuration.setHost(host);
                Configuration.setPort(port);

        ScriptManager scriptManager = new ScriptManager();
        ClientManager clientManager = new ClientManager();

        Runner runner = new Runner(console, clientManager, scriptManager);

        new GuiManager(null, clientManager, runner).run();
    }
}
