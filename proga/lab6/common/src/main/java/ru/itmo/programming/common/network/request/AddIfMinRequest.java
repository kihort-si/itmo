package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.utils.Commands;

public class AddIfMinRequest extends Request {
    private final Person person;
    public AddIfMinRequest(Person person) {
        super(Commands.ADD_IF_MIN.getName());
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
