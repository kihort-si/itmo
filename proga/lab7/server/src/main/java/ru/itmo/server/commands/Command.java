package ru.itmo.server.commands;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.Response;

import java.util.Objects;

/**
 * @author Nikita Vasilev
 */
public abstract class Command implements Executable {
    private final String name;
    private final String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Command: " + getName() + " (" + getDescription() + ").";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Command command = (Command) object;
        return Objects.equals(getName(), command.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * @return command name
     */
    public String getName() {
        return name;
    }

    /**
     * @return command description
     */
    public String getDescription() {
        return description;
    }

    public abstract Response execute(Request request);
}
