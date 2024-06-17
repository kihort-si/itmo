package ru.itmo.server.handlers;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.NoSuchCommandResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.server.commands.Command;
import ru.itmo.server.managers.CommandManager;

/**
 * @author Nikita Vasilev
 */
public class CommandHandler {
    private final CommandManager commandManager;
    public CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * @param request An incoming request from a client to execute a command.
     * @return Response to the corresponding command, if any, or a message that the command does not exist.
     */
    public Response handler(Request request) {
        Command command = commandManager.getCommandMap().get(request.getName());
        if (command == null) {
            return new NoSuchCommandResponse(request.getName(), 0);
        } else return command.execute(request);
    }
}
