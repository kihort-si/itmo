package ru.itmo.server.commands;

import ru.itmo.common.network.request.RegisterRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.RegisterResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.UserManager;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;

public class Register extends Command {
    private final UserManager userManager;
    public Register(UserManager userManager) {
        super(Commands.REGISTER.getName(), Commands.REGISTER.getDescription());
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (RegisterRequest) request;
            if (userManager.checkUser(req.getUser().getLogin())) {
                return new RegisterResponse(null, "Пользователь с таким логином уже существует", 1);
            } else {
                int id = userManager.addUser(req.getUser());
                req.getUser().setId(id);
                return new RegisterResponse(req.getUser().getLogin(), null, 0);
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            return new RegisterResponse(null, e.getMessage(), 2);
        }
    }
}
