package ru.itmo.server.commands;

import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.AddIfMinRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.AddIfMinResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class AddIfMin extends Command {
    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;

    public AddIfMin(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.ADD_IF_MIN.getName(), Commands.ADD_IF_MIN.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AddIfMinRequest) request;
            double minHeight = minHeight();
            if (req.getPerson().getHeight() > minHeight) {
                if (!req.getPerson().verificate()) {
                    return new AddIfMinResponse(-1, false, "Человек не добавлен, так как поля не валидны!", 1);
                } else if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                    return new AddIfMinResponse(-1, false, "Вы не вошли. Используйте команду Authorization", 2);
                } else {
                    long nextId = databaseManger.addPerson(req.getPerson());

                    collectionVault.addToCollection(nextId, req.getPerson(), UserManager.getCurrentUser());
                    return new AddIfMinResponse(nextId, true, null, 0);
                }
            } else {
                return new AddIfMinResponse(-1, false, "Человек не добавлен, так как его рост не является максимальным", 3);
            }
        } catch (Exception e) {
            return new AddIfMinResponse(-1, false, e.toString(), 4);
        }
    }

    public double minHeight() {
        return collectionVault.getCollection().stream().map(Person::getHeight).mapToDouble(Double::doubleValue).min().orElse(-1);
    }
}