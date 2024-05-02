package ru.itmo.programming.common.network.response;

public class NoSuchCommandResponse extends Response {
    public NoSuchCommandResponse(String name) {
        super(name, "Неизвестная команда");
    }
}
