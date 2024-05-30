package ru.itmo.server.commands;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.Response;

public interface Executable {
    Response execute(Request request);
}
