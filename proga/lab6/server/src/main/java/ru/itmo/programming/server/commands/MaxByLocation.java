package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.MaxByLocationRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.MaxByLocationResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.exceptions.EmptyCollectionException;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class MaxByLocation extends Command {
    private final CollectionVault collectionVault;
    public MaxByLocation(CollectionVault collectionVault) {
        super("max_by_location", "вывести любой объект из коллекции, значение поля location которого является максимальным");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (MaxByLocationRequest) request;
            if (collectionVault.getCollection().isEmpty()) {
                throw new EmptyCollectionException("Коллекция пуста.");
            } else {
                return new MaxByLocationResponse(collectionVault.maxLocation(), null);
            }
        } catch (Exception e) {
            return new MaxByLocationResponse(null, e.toString());
        }
    }
}
