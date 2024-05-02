package ru.itmo.programming.server.network;

public class Configuration {
    private static String host;
    private static int port;

    /**
     * @return The host through which the server is connected.
     */
    public static String getHost() {
        return host;
    }

    /**
     * @param host The host that is set up for server connectivity.
     */
    public static void setHost(String host) {
        Configuration.host = host;
    }

    /**
     * @return The port through which the server is connected.
     */
    public static int getPort() {
        return port;
    }

    /**
     * @param port The port that is set up for server connectivity.
     */
    public static void setPort(int port) {
        Configuration.port = port;
    }
}
