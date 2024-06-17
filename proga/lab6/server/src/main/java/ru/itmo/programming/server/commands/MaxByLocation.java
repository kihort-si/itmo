package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.MaxByLocationResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.exceptions.EmptyCollectionException;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class MaxByLocation extends Command {
    private final CollectionVault collectionVault;

    public MaxByLocation(CollectionVault collectionVault) {
        super(Commands.MAX_BY_LOCATION.getName(), Commands.MAX_BY_LOCATION.getDescription());
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
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
