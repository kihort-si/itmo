package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.RemoveByIdRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.RemoveByIdResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class RemoveById extends Command {
    private final CollectionVault collectionVault;
    public RemoveById(CollectionVault collectionVault) {
        super("remove_by_id", "удалить элемент из коллекции по его id");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (RemoveByIdRequest) request;

            if (!collectionVault.existId(req.getId())) {
                return new RemoveByIdResponse("Человека с таким ID не найдено");
            } else {
                collectionVault.removeFromCollection(req.getId());
                return new RemoveByIdResponse(null);
            }
        } catch (Exception e) {
            return new RemoveByIdResponse(e.toString());
        }
    }
}
