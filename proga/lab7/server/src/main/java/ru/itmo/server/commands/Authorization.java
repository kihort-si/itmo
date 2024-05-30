package ru.itmo.server.commands;

import ru.itmo.common.network.request.AuthorizationRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.AuthorizationResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.UserManager;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class Authorization extends Command {
    private final UserManager userManager;
    public Authorization(UserManager userManager) {
        super(Commands.AUTHORIZATION.getName(), Commands.AUTHORIZATION.getDescription());
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AuthorizationRequest) request;
            if (!userManager.checkUser(req.getLogin())) {
                return new AuthorizationResponse(null, "Пользователь с логином " + req.getLogin() + " не найден");
            } else if (!userManager.checkPassword(req.getLogin(), req.getPassword())) {
                return new AuthorizationResponse(null, "Введен неверный пароль");
            } else {
                int id = userManager.getUser(req.getLogin());
                UserManager.setCurrentUser(id);
                return new AuthorizationResponse("Выполнена авторизация!", null);
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            return new AuthorizationResponse(null, "Произошла ошибка при работе с базой данных");
        } catch (Exception e) {
            return new AuthorizationResponse(null, e.toString());
        }
    }
}
