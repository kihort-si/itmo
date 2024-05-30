package ru.itmo.server.commands;

import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.AddIfMaxRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.AddIfMaxResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.DatabaseManger;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class AddIfMax extends Command {
    private final CollectionVault collectionVault;
    private final DatabaseManger databaseManger;
    private final UserManager userManager;

    public AddIfMax(CollectionVault collectionVault, DatabaseManger databaseManger, UserManager userManager) {
        super(Commands.ADD_IF_MAX.getName(), Commands.ADD_IF_MAX.getDescription());
        this.collectionVault = collectionVault;
        this.databaseManger = databaseManger;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AddIfMaxRequest) request;
            double maxHeight = maxHeight();
            if (req.getPerson().getHeight() > maxHeight) {
                if (!req.getPerson().verificate()) {
                    return new AddIfMaxResponse(-1, false, "Человек не добавлен, так как поля не валидны!");
                } else if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                    return new AddIfMaxResponse(-1, false, "Вы не вошли. Используйте команду Authorization");
                } else {
                    long nextId = databaseManger.addPerson(req.getPerson());

                    collectionVault.addToCollection(nextId, req.getPerson(), UserManager.getCurrentUser());
                    return new AddIfMaxResponse(nextId, true, null);
                }
            } else {
                return new AddIfMaxResponse(-1, false, "Человек не добавлен, так как его рост не является максимальным");
            }
        } catch (Exception e) {
            return new AddIfMaxResponse(-1, false, e.toString());
        }
    }

    public double maxHeight() {
        return collectionVault.getCollection().stream().map(Person::getHeight).mapToDouble(Double::doubleValue).max().orElse(-1);
    }
}
