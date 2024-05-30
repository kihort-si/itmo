package ru.itmo.server.commands;

import ru.itmo.common.network.request.RemoveByIdRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.RemoveByIdResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class RemoveById extends Command {
    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;
    public RemoveById(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.REMOVE_BY_ID.getName(), Commands.REMOVE_BY_ID.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (RemoveByIdRequest) request;

            if (!collectionVault.existId(req.getId())) {
                return new RemoveByIdResponse("Человека с таким ID не найдено");
            } if (UserManager.getCurrentUser() != collectionVault.getById(req.getId()).getCreator()) {
                return new RemoveByIdResponse("Человек с ID " + req.getId() + " создан другим пользователем");
            } else if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new RemoveByIdResponse("Вы не вошли. Используйте команду Authorization");
            } else {
                databaseManger.removePerson(req.getId(), UserManager.getCurrentUser());

                collectionVault.removeFromCollection(req.getId(), UserManager.getCurrentUser());
                return new RemoveByIdResponse(null);
            }
        } catch (Exception e) {
            return new RemoveByIdResponse(e.toString());
        }
    }
}
