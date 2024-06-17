package ru.itmo.common.network.response;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

import java.util.List;

public class ShowResponse extends Response {
    private final List<Person> people;

    public ShowResponse(List<Person> people, String error, int status) {
        super(Commands.SHOW.getName(), error, status);
        this.people = people;
    }

    public List<Person> getPeople() {
        return people;
    }
}
