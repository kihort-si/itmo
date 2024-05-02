package ru.itmo.programming.server.commands;


import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.request.UpdateIdRequest;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.network.response.UpdateIdResponse;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class UpdateId extends Command {

    private final CollectionVault collectionVault;

    public UpdateId(CollectionVault collectionVault) {
        super("update_id", "обновить значение элемента коллекции, id которого равен заданному");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (UpdateIdRequest) request;
            if (!collectionVault.existId(req.getId())) {
                return new UpdateIdResponse("Человека с таким ID не существует");
            }
            if (!req.getUpdatedPerson().verificate()) {
                return new UpdateIdResponse("Человек не добавлен, так как поля не валидны!");
            }
            collectionVault.getById(req.getId()).update(req.getUpdatedPerson());
            return new UpdateIdResponse(null);
        } catch (Exception e) {
            return new UpdateIdResponse(e.toString());
        }
    }
}
