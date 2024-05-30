package ru.itmo.server.commands;

import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.CountGreaterThanWeightRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.CountGreaterThanWeightResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class CountGreaterThanWeight extends Command {
    private final CollectionVault collectionVault;
    private final UserManager userManager;
    public CountGreaterThanWeight(CollectionVault collectionVault, UserManager userManager) {
        super(Commands.COUNT_GREATER_THAN_WEIGHT.getName(), Commands.COUNT_GREATER_THAN_WEIGHT.getDescription());
        this.collectionVault = collectionVault;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new CountGreaterThanWeightResponse(null, "Вы не вошли. Используйте команду Authorization");
            } else {
                var req = (CountGreaterThanWeightRequest) request;
                return new CountGreaterThanWeightResponse(counter(req.getWeight()), null);
            }
        } catch (Exception e) {
            return new CountGreaterThanWeightResponse(null, e.toString());
        }
    }

    private int counter(double weight) {
        return (int) collectionVault.getCollection().stream()
                .filter(person -> person.getWeight() > weight)
                .count();
    }
}
