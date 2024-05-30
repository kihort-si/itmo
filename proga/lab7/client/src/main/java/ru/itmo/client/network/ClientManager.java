package ru.itmo.client.network;

import org.slf4j.Logger;
import ru.itmo.client.App;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.Response;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class ClientManager {
    Client client;
    private final Logger logger = App.logger;

    public ClientManager() {
        client = new Client(Configuration.getHost(), Configuration.getPort());
    }

    /**
     * Forming, sending a request and receiving a response from the server to transmit a command and receive information about its execution.
     * @param request A command received from the user to be passed to the server as a request.
     * @return A response received from the server containing information about the execution of a command or the necessary data requested by the user.
     * @throws IOException If an error occurs when creating, transferring, or closing a socket.
     * @throws ClassNotFoundException If the socket transmission failed to find the class of the object being transmitted.
     */
    public Response sendAndReceiveCommand(Request request) throws IOException, ClassNotFoundException {
        client.start();

        client.writeObject(request);
        Response response = (Response) client.getObject();
        client.close();

        logger.info("Получен ответ от сервера: " + response);
        return response;
    }
}
