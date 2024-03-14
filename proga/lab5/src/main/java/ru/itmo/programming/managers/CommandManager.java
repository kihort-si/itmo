package ru.itmo.programming.managers;

import ru.itmo.programming.commands.Command;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nikita Vasilev
 */
public class CommandManager {
    private final Map<String, Command> commandMap = new HashMap<>();

    /**
     * @param command command to be created for program operation
     */
    public void createCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    /**
     * @return list of commands available for use
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    /**
     *
     * @param name command name
     * @return command name
     */
    public Command getCommand(String name) {
        return commandMap.get(name);
    }
}
