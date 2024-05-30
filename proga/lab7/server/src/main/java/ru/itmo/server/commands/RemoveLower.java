package ru.itmo.server.commands;

import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.RemoveLowerRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.RemoveLowerResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nikita Vasilev
 */
public class RemoveLower extends Command {
    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;

    public RemoveLower(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.REMOVE_LOWER.getName(), Commands.REMOVE_LOWER.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (RemoveLowerRequest) request;

            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new RemoveLowerResponse(null, "Вы не вошли. Используйте команду Authorization");
            } else {
                databaseManger.removeLower(req.getHeight(), UserManager.getCurrentUser());

                return new RemoveLowerResponse(remove(req.getHeight()), null);
            }
        } catch (Exception e) {
            return new RemoveLowerResponse(null, e.getMessage());
        }
    }

    public int remove(double height) {
        Set<Long> lowerElements = collectionVault.getCollection().stream()
                .filter(person -> person.getHeight() < height)
                .map(Person::getId)
                .collect(Collectors.toSet());

        lowerElements.forEach(id -> collectionVault.removeFromCollection(id, UserManager.getCurrentUser()));

        return lowerElements.size();
    }
}
