package ru.itmo.common.network.request;

import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.Commands;

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
