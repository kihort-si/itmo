package ru.itmo.programming.common.utils;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A list of commands available for execution.
 * @author Nikita Vasilev
 */
public enum Commands {
    ADD("add", "добавить новый элемент в коллекцию"),
    ADD_IF_MAX("add_if_max", "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"),
    ADD_IF_MIN("add_if_min", "добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции"),
    CLEAR("clear", "очистить коллекцию"),
    COUNT_GREATER_THAN_WEIGHT("count_greater_than_weight", "вывести количество элементов, значение поля weight которых больше заданного"),
    EXECUTE_SCRIPT("execute_script", "считать и исполнить скрипт из указанного файла"),
    EXIT("exit", "завершить программу"),
    FILTER_LESS_THAN_HEIGHT("filter_less_than_height", "вывести элементы, значение поля height которых меньше заданного"),
    HELP("help", "вывести справку по доступным командам"),
    INFO("info", "вывести в стандартный поток вывода информацию о коллекции"),
    MAX_BY_LOCATION("max_by_location", "вывести любой объект из коллекции, значение поля location которого является максимальным"),
    REMOVE_BY_ID("remove_by_id", "удалить элемент из коллекции по его id"),
    REMOVE_LOWER("remove_lower", "удалить из коллекции все элементы, меньшие, чем заданный"),
    SHOW("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"),
    UPDATE_ID("update_id", "обновить значение элемента коллекции, id которого равен заданному");

    private final String name;
    private final String description;

    Commands(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return list of commands consisting of the pair name + description
     */
    public static Map<String, String> getCommands() {
        return EnumSet.allOf(Commands.class).stream().collect(Collectors.toMap(Commands::getName, Commands::getDescription));
    }
}
