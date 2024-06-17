package ru.itmo.programming.common.network.response;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.utils.Commands;

import java.util.Optional;

public class MaxByLocationResponse extends Response {
    private final Person maxByLocationPerson;

    public MaxByLocationResponse(Person maxByLocationPerson, String error) {
        super(Commands.MAX_BY_LOCATION.getName(), error);
        this.maxByLocationPerson = maxByLocationPerson;
    }

    public Person getMaxByLocationPerson() {
        return maxByLocationPerson;
    }
}
