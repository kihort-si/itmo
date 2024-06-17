package ru.itmo.common.network.response;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

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
