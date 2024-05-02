package ru.itmo.programming.server.network;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.server.App;
import ru.itmo.programming.server.handlers.CommandHandler;
import ru.itmo.programming.server.managers.CommandManager;
import ru.itmo.programming.server.managers.FileManager;
import ru.itmo.programming.server.vaults.CollectionVault;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ServerManager {
    private final Server server;
    private final CommandManager commandManager;
    private final CommandHandler commandHandler;
    private final FileManager fileManager;
    private final CollectionVault collectionVault;
    private final Console console;

    private Runnable afterHook;

    public ServerManager(CommandManager commandManager, FileManager fileManager, CollectionVault collectionVault, Console console) {
        this.fileManager = fileManager;
        this.collectionVault = collectionVault;
        this.console = console;
        server = new Server(Configuration.getHost(), Configuration.getPort());
        this.commandManager = commandManager;
        commandHandler = new CommandHandler(this.commandManager);
    }

    /**
     * Server startup.
     * @throws IOException If an error occurred when starting the server.
     */
    public void start() throws IOException {
        server.run();
    }

    /**
     * Transferring data from the server to the client.
     * @param socketChannel The current network channel on which data is being transmitted.
     * @param response A response sent from the server to the client.
     */
    public void writeRes(SocketChannel socketChannel, Response response) {
        try {
            server.writeObject(socketChannel, response);
        } catch (IOException e) {
            App.logger.error("Не удалось передать данные");
        }
    }

    /**
     * Formation of an answer based on the request received from the client.
     * @param socketChannel The current network channel on which data is being transmitted.
     * @throws IOException If a data transmission error occurs.
     */
    public void handlerSocketChanel(SocketChannel socketChannel) throws IOException {
        Request request;
        try {
            request = (Request) server.getObject(socketChannel);
            Response response = commandHandler.handler(request);
            if (afterHook != null) afterHook.run();
            writeRes(socketChannel, response);
        } catch (IOException | ClassNotFoundException e) {
            App.logger.error(e.toString());
            socketChannel.close();
        } catch (ClassCastException e) {
            App.logger.error(e.toString());
        } finally {
            socketChannel.close();
        }
    }

    /**
     * Starting server operation.
     */
    public void run() {
        SocketChannel socketChannel;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                if (console.hasNewln()) {
                    String input = console.readln();
                    if (input.equals("save")){
                        fileManager.writeCollection(collectionVault.getCollection());
                        App.logger.info("Коллекция успешно сохранена!");
                    } else if (input.equals("exit")) {
                        fileManager.writeCollection(collectionVault.getCollection());
                        App.logger.info("Коллекция успешно сохранена!");
                        System.exit(1);
                    } else {
                        App.logger.error("Неизвестная команда");
                    }
                }
            } catch (IOException e) {
                App.logger.error("Неизвестная ошибка");
            }
            try {
                socketChannel = server.getSocket();
                if (socketChannel == null) continue;
                handlerSocketChanel(socketChannel);
            } catch (IOException e) {
                App.logger.error(e.toString());
            }
        }
    }

    /**
     * @param afterHook A boolean value whether program termination has occurred.
     */
    public void setAfterHook(Runnable afterHook) {
        this.afterHook = afterHook;
    }
}
