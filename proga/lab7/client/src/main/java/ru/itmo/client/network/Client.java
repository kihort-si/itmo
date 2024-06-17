package ru.itmo.client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Nikita Vasilev
 */
public class Client {
    private final String host;
    private final int port;
    private Socket socket;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Socket mating with the specified port and host.
     *
     * @throws IOException If an error occurs when creating a socket.
     */
    public void start() throws IOException {
        socket = new Socket(host, port);
    }

    /**
     * An input stream on the client that accepts objects from the server.
     *
     * @return An object received from the server.
     * @throws IOException            If an error occurs during socket transfer.
     * @throws ClassNotFoundException If the socket transmission failed to find the class of the object being transmitted.
     */
    public Object getObject() throws IOException, ClassNotFoundException {
        synchronized (socket) {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            return ois.readObject();
        }
    }

    /**
     * An output stream on the client that passes objects to the servers.
     *
     * @param obj The object to be transferred to the server.
     * @throws IOException If an error occurs during socket transfer.
     */
    public void writeObject(Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(obj);
    }

    /**
     * Closing a thread to transmit sockets.
     *
     * @throws IOException If an error occurs when closing a socket.
     */
    public void close() throws IOException {
        socket.close();
    }
}
