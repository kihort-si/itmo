package ru.itmo.common.network.response;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

import java.util.List;

public class ShowResponse extends Response {
    private final List<Person> people;

    public ShowResponse(List<Person> people, String error) {
        super(Commands.SHOW.getName(), error);
        this.people = people;
    }

    public List<Person> getPeople() {
        return people;
    }
}
