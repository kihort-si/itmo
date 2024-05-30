package ru.itmo.server.commands;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.ClearResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita
 */
public class Clear extends Command {
    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;

    public Clear(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.CLEAR.getName(), Commands.CLEAR.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new ClearResponse("Вы не вошли. Используйте команду Authorization");
            } else {
                databaseManger.clear();

                collectionVault.clearCollection(UserManager.getCurrentUser());
                return new ClearResponse(null);
            }
        } catch (Exception e) {
            return new ClearResponse(e.toString());
        }
    }
}
