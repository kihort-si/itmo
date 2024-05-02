package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.utils.Commands;

public class AddRequest extends Request {
    private final Person person;

    public AddRequest(Person person) {
        super(Commands.ADD.getName());
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
