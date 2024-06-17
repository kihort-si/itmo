package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.network.response.ShowResponse;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class Show extends Command {
    private final CollectionVault collectionVault;

    public Show(CollectionVault collectionVault) {
        super(Commands.SHOW.getName(), Commands.SHOW.getDescription());
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            return new ShowResponse(collectionVault.getSortedCollection(), null);
        } catch (Exception e) {
            return new ShowResponse(null, e.toString());
        }
    }
}
