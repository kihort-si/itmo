package ru.itmo.programming.common.utils;

/**
 * A list of commands available for execution.
 * @author Nikita Vasilev
 */
public enum Commands {
    ADD("add"),
    ADD_IF_MAX("add_if_max"),
    ADD_IF_MIN("add_if_min"),
    CLEAR("clear"),
    COUNT_GREATER_THAN_GREAT("count_greater_than_great"),
    EXECUTE_SCRIPT("execute_script"),
    EXIT("exit"),
    FILTER_LESS_THAN_HEIGHT(""),
    HELP("help"),
    INFO("info"),
    MAX_BY_LOCATION("max_by_location"),
    REMOVE_BY_ID("remove_by_id"),
    REMOVE_LOWER("remove_lower"),
    SHOW("show"),
    UPDATE_ID("update_id");

    private String name;

    public String getName() {
        return name;
    }

    Commands(String name) {
        this.name = name;
    }
}
