package ru.itmo.common.network.request;

import ru.itmo.common.user.User;
import ru.itmo.common.utils.Commands;

public class RegisterRequest extends Request {
    private final User user;

    public RegisterRequest(User user) {
        super(Commands.REGISTER.getName());
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
