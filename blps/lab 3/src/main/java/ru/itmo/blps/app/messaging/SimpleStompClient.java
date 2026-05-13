package ru.itmo.blps.app.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class SimpleStompClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final String login;
    private final String passcode;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public SimpleStompClient(String host, int port, String login, String passcode) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.login = login == null ? "" : login;
        this.passcode = passcode == null ? "" : passcode;
    }

    public synchronized void sendJson(String stompDestination, String jsonBody) throws IOException {
        connectIfNeeded();
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        StringBuilder frame = new StringBuilder();
        frame.append("SEND\n");
        frame.append("destination:").append(stompDestination).append('\n');
        frame.append("content-type:application/json\n");
        frame.append("content-length:").append(bodyBytes.length).append("\n\n");
        out.write(frame.toString().getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.write(0);
        out.flush();
    }

    private void connectIfNeeded() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }
        socket = new Socket(host, port);
        socket.setSoTimeout(30_000);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        StringBuilder connect = new StringBuilder();
        connect.append("CONNECT\n");
        connect.append("accept-version:1.1,1.0\n");
        connect.append("host:").append(host).append('\n');
        if (!login.isBlank()) {
            connect.append("login:").append(login).append('\n');
            connect.append("passcode:").append(passcode).append('\n');
        }
        connect.append('\n').append('\0');
        out.write(connect.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
        readStompFrame();
    }

    private void readStompFrame() throws IOException {
        ByteArrayOutputStream acc = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) >= 0) {
            if (b == 0) {
                break;
            }
            acc.write(b);
        }
        String frame = acc.toString(StandardCharsets.UTF_8);
        if (!frame.startsWith("CONNECTED")) {
            if (frame.startsWith("ERROR")) {
                throw new IOException("STOMP ERROR: " + frame);
            }
            throw new IOException("Unexpected STOMP frame: " + frame);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (out != null && socket != null && socket.isConnected()) {
            try {
                out.write("DISCONNECT\n\n\0".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException ignored) {
            }
        }
        if (socket != null) {
            socket.close();
        }
        socket = null;
        in = null;
        out = null;
    }
}
