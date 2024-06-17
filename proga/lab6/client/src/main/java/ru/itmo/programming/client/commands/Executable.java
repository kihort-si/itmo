package ru.itmo.programming.client.commands;

/**
 * @author Nikita Vasilev
 */
public interface Executable {
    /**
     * An abstract method whose implementation is required for each created command. The method allows to define the implementation of a command after user input or when reading from a file.
     * @param args a string of data entered from the console or from a script, specifying the command and the necessary arguments
     */
    void execute(String[] args);
}
