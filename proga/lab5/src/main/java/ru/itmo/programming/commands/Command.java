package ru.itmo.programming.commands;

import java.util.Objects;

/**
 * @author Nikita Vasilev
 */
public abstract class Command {
    private final String name;
    private final String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * @param args a string of data entered from the console or from a script, specifying the command and the necessary arguments
     */
    public abstract void execute(String[] args);

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
}
