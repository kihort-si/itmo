package ru.itmo.server.network;

public class Configuration {
    private static String host;
    private static int port;
    private static String url;
    private static String login;
    private static String password;

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

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        Configuration.url = url;
    }

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        Configuration.login = login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Configuration.password = password;
    }
}
