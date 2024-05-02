package ru.itmo.programming.client.commands;

/**
 * @author Nikita Vasilev
 */
public interface Validateable {
    /**
     * A void that checks if the command arguments are entered correctly.
     * @param args String representation of arguments passed by the user.
     * @return A boolean value that determines whether the correct number of arguments is accepted for a particular command.
     */
    boolean validateArgs(String[] args);
}
