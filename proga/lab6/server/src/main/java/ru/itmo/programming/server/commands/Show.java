package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.network.response.ShowResponse;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class Show extends Command {
    private final CollectionVault collectionVault;
    public Show(CollectionVault collectionVault) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
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
