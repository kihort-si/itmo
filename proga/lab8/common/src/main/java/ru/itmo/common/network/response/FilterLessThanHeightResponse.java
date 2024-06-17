package ru.itmo.common.network.response;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

import java.util.List;

public class FilterLessThanHeightResponse extends Response {
    private final List<Person> filtredList;
    public FilterLessThanHeightResponse(List<Person> filtredList, String error, int status) {
        super(Commands.FILTER_LESS_THAN_HEIGHT.getName(), error, status);
        this.filtredList = filtredList;
    }

    public List<Person> getFiltredList() {
        return filtredList;
    }
}
