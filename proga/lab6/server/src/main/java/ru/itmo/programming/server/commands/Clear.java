package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.ClearResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita
 */
public class Clear extends Command {
    private final CollectionVault collectionVault;
    public Clear(CollectionVault collectionVault) {
        super("clear", "очистить коллекцию");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            collectionVault.clearCollection();
            return new ClearResponse(null);
        } catch (Exception e) {
            return new ClearResponse(e.toString());
        }
    }
}
