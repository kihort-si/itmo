package ru.itmo.server.network;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Console;
import ru.itmo.server.App;
import ru.itmo.server.handlers.CommandHandler;
import ru.itmo.server.managers.CommandManager;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ServerManager {
    private final Server server;
    private final CommandHandler commandHandler;
    private final Console console;
    private final ExecutorService readThreadPool = Executors.newCachedThreadPool();
    private final ForkJoinPool processThreadPool = new ForkJoinPool();

    public ServerManager(CommandManager commandManager, Console console) {
        this.console = console;
        server = new Server(Configuration.getHost(), Configuration.getPort());
        commandHandler = new CommandHandler(commandManager);
    }

    /**
     * Server startup.
     *
     * @throws IOException If an error occurred when starting the server.
     */
    public void start() throws IOException {
        server.run();
    }

    /**
     * Transferring data from the server to the client.
     *
     * @param socketChannel The current network channel on which data is being transmitted.
     * @param response      A response sent from the server to the client.
     */
    public void writeRes(SocketChannel socketChannel, Response response) {
        new Thread(() -> {
            synchronized (socketChannel) {
                try {
                    server.writeObject(socketChannel, response);
                } catch (IOException e) {
                    App.logger.error("Не удалось передать данные: " + e.getMessage());
                    try {
                        socketChannel.close();
                    } catch (IOException ex) {
                        App.logger.error("Ошибка при закрытии сокета: " + ex.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Formation of an answer based on the request received from the client.
     *
     * @param socketChannel The current network channel on which data is being transmitted.
     * @throws IOException If a data transmission error occurs.
     */
    public void handlerSocketChannel(SocketChannel socketChannel) throws IOException {
        try {
            Request request = (Request) server.getObject(socketChannel);
            processThreadPool.submit(() -> {
                try {
                    Response response = commandHandler.handler(request);
                    writeRes(socketChannel, response);
                } catch (Exception e) {
                    App.logger.error("Ошибка при обработке запроса: " + e.getMessage());
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            App.logger.error("Ошибка при чтении объекта: " + e.getMessage());
            socketChannel.close();
        } catch (ClassCastException e) {
            App.logger.error("Ошибка приведения типа: " + e.getMessage());
        }
    }

    /**
     * Starting server operation.
     */
    public void run() {
        while (true) {
            try {
                if (console.hasNewln()) {
                    String input = console.readln();
                    if (input.equals("exit")) {
                        App.logger.info("Работа сервера завершена");
                        System.exit(1);
                    } else {
                        App.logger.error("Неизвестная команда");
                    }
                }
            } catch (IOException e) {
                App.logger.error("Неизвестная ошибка при чтении из консоли");
            }
            try {
                SocketChannel socketChannel = server.getSocket();
                if (socketChannel == null) continue;
                readThreadPool.submit(() -> {
                    try {
                        handlerSocketChannel(socketChannel);
                    } catch (IOException e) {
                        App.logger.error("Ошибка при обработке сокет-канала: " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                App.logger.error("Ошибка при получении сокета: " + e.getMessage());
            }
        }
    }
}
