package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.AddRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.AddResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class Add extends Command {
    private final CollectionVault collectionVault;
    public Add(CollectionVault collectionVault) {
        super("add", "добавить новый элемент в коллекцию");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AddRequest) request;
            if (!req.getPerson().verificate()) {
                return new AddResponse(null, "Человек не добавлен, так как поля не валидны!");
            } else {
                long nextId = collectionVault.freeIds();
                collectionVault.addToCollection(nextId, req.getPerson());
                return new AddResponse(nextId, null);
            }
        } catch (Exception e) {
            return new AddResponse(null, e.toString());
        }
    }
}
