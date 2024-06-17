package ru.itmo.server.commands;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.network.response.ShowResponse;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class Show extends Command {
    private final CollectionVault collectionVault;
    private final UserManager userManager;

    public Show(CollectionVault collectionVault, UserManager userManager) {
        super(Commands.SHOW.getName(), Commands.SHOW.getDescription());
        this.collectionVault = collectionVault;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new ShowResponse(null, "Вы не вошли. Используйте команду Authorization", 1);
            } else {
                return new ShowResponse(collectionVault.getSortedCollection(), null, 0);
            }
        } catch (Exception e) {
            return new ShowResponse(null, e.toString(), 2);
        }
    }
}
