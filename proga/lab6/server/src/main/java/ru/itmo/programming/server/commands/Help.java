package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.HelpResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.server.managers.CommandManager;

/**
 * @author Nikita Vasilev
 */
public class Help extends Command {
    private final CommandManager commandManager;
    public Help(CommandManager commandManager) {
        super("help", "вывести справку по доступным командам");
        this.commandManager = commandManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            StringBuilder helpMessage = new StringBuilder();

            commandManager.getCommandMap().values().forEach(command -> {
                helpMessage.append(command.getName()).append(": ").append(command.getDescription());
            });

            return new HelpResponse(helpMessage.toString(), null);
        } catch (Exception e) {
            return new HelpResponse(null, e.toString());
        }
    }
}
