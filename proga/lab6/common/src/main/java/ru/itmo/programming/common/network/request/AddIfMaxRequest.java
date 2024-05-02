package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.utils.Commands;

public class AddIfMaxRequest extends Request {
    private final Person person;
    public AddIfMaxRequest(Person person) {
        super(Commands.ADD_IF_MAX.getName());
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
