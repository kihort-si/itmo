package ru.itmo.common.network.response;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

import java.util.Optional;

public class MaxByLocationResponse extends Response {
    private final Optional<Person> maxByLocationPerson;
    public MaxByLocationResponse(Optional<Person> maxByLocationPerson, String error) {
        super(Commands.MAX_BY_LOCATION.getName(), error);
        this.maxByLocationPerson = maxByLocationPerson;
    }

    public Optional<Person> getMaxByLocationPerson() {
        return maxByLocationPerson;
    }
}
