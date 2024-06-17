package ru.itmo.common.network.response;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

import java.util.Optional;

public class MaxByLocationResponse extends Response {
    private final Person maxByLocationPerson;
    public MaxByLocationResponse(Person maxByLocationPerson, String error, int status) {
        super(Commands.MAX_BY_LOCATION.getName(), error, status);
        this.maxByLocationPerson = maxByLocationPerson;
    }

    public Person getMaxByLocationPerson() {
        return maxByLocationPerson;
    }
}
