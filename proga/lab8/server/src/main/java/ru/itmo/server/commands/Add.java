package ru.itmo.server.commands;

import ru.itmo.common.network.request.AddRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.AddResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class Add extends Command {
    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;
    public Add(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.ADD.getName(), Commands.ADD.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AddRequest) request;
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser())) || UserManager.getCurrentUser() == 0) {
                return new AddResponse(-1, "Вы не вошли. Используйте команду Authorization", 1);
            } else if (!req.getPerson().verificate()) {
                return new AddResponse(-1, "Человек не добавлен, так как поля не валидны!", 2);
            } else {
                Long nextId = databaseManger.addPerson(req.getPerson());

                if (nextId == null) {
                    return new AddResponse(-1, "Ошибка при добавлении человека в базу данных.", 3);
                } else {
                    collectionVault.addToCollection(nextId, req.getPerson(), UserManager.getCurrentUser());
                    return new AddResponse(nextId, null, 0);
                }
            }
        } catch (Exception e) {
            return new AddResponse(-1, e.toString(), 4);
        }
    }
}
