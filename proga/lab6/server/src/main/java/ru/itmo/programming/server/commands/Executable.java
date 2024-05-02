package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.Response;

public interface Executable {
    Response execute(Request request);
}
