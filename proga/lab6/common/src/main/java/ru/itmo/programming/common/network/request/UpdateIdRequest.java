package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.utils.Commands;

public class UpdateIdRequest extends Request {

    private final long id;
    private final Person updatedPerson;

    public UpdateIdRequest(long id, Person updatedPerson) {
        super(Commands.UPDATE_ID.getName());
        this.id = id;
        this.updatedPerson = updatedPerson;
    }

    public long getId() {
        return id;
    }

    public Person getUpdatedPerson() {
        return updatedPerson;
    }
}
