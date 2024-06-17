package ru.itmo.common.network.response;

public class NoSuchCommandResponse extends Response {
    public NoSuchCommandResponse(String name, int status) {
        super(name, "Неизвестная команда", status);
    }
}
