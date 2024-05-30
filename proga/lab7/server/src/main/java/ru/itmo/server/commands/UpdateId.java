package ru.itmo.server.commands;


import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.request.UpdateIdRequest;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.network.response.UpdateIdResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class UpdateId extends Command {

    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;

    public UpdateId(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.UPDATE_ID.getName(), Commands.UPDATE_ID.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (UpdateIdRequest) request;
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new UpdateIdResponse("Вы не вошли. Используйте команду Authorization");
            }
            if (!collectionVault.existId(req.getId())) {
                return new UpdateIdResponse("Человека с таким ID не существует");
            }
            if (UserManager.getCurrentUser() != collectionVault.getById(req.getId()).getCreator()) {
                return new UpdateIdResponse("Человек с ID " + req.getId() + " создан другим пользователем");
            }
            if (!req.getUpdatedPerson().verificate()) {
                return new UpdateIdResponse("Человек не обновлен, так как поля не валидны!");
            }
            databaseManger.updatePerson(req.getUpdatedPerson());

            collectionVault.getById(req.getId()).update(req.getUpdatedPerson());
            return new UpdateIdResponse(null);
        } catch (Exception e) {
            return new UpdateIdResponse(e.toString());
        }
    }
}
