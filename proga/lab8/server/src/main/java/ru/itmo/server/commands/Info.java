package ru.itmo.server.commands;

import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.InfoResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * The Info class is responsible for getting information about the state of the collection and generating a response to send to the client.
 * @author Nikita Vasilev
 */
public class Info extends Command {
    private final CollectionVault collectionVault;
    private final UserManager userManager;

    public Info(CollectionVault collectionVault, UserManager userManager) {
        super(Commands.INFO.getName(), Commands.INFO.getDescription());
        this.collectionVault = collectionVault;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new InfoResponse(null, "Вы не вошли. Используйте команду Authorization", 1);
            } else {
                String[] message = {
                        collectionVault.getCollectionType(),
                        collectionVault.getElementsType(),
                        String.valueOf(collectionVault.getInitializationDate()),
                        String.valueOf(collectionVault.getCollectionSize())};
                return new InfoResponse(message, null, 0);
            }
        } catch (Exception e) {
            return new InfoResponse(null, e.toString(), 2);
        }
    }
}