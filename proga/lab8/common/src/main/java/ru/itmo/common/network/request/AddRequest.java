package ru.itmo.common.network.request;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

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
