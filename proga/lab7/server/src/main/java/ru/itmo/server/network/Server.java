package ru.itmo.server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Nikita Vasilev
 */
public class Server {
    private final String host;
    private final int port;
    private ServerSocketChannel server;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Starts a network channel for server operation.
     *
     * @throws IOException If an error occurs when opening a channel.
     */
    public void run() throws IOException {
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(host, port));
    }

    /**
     * Retrieving the current socket.
     *
     * @return The socket channel for the new connection, or null if this channel is in non-blocking mode and no connection is available to be accepted.
     * @throws IOException If an unexpected error occurred while receiving the current socket.
     */
    public SocketChannel getSocket() throws IOException {
        return server.accept();
    }

    /**
     * Retrieving objects from the client using a network channel.
     *
     * @param socketChannel The current channel on which objects are transmitted.
     * @return An object received from the client.
     * @throws IOException            If an error occurs during socket transfer.
     * @throws ClassNotFoundException If the socket transmission failed to find the class of the object being transmitted.
     */
    public Object getObject(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(socketChannel.socket().getInputStream());
        return ois.readObject();
    }

    /**
     * @param socketChannel The current channel on which objects are written.
     * @param obj           The object to be returned to the client.
     * @throws IOException If an error occurs during socket transfer.
     */
    public void writeObject(SocketChannel socketChannel, Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(socketChannel.socket().getOutputStream());
        oos.writeObject(obj);
    }

    /**
     * Closing the socket for server operation.
     *
     * @throws IOException If an error occurred when closing the socket.
     */
    public void close() throws IOException {
        server.close();
    }
}
