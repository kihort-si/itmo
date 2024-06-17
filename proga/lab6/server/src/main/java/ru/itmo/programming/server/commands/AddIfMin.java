package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.network.request.AddIfMinRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.AddIfMinResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class AddIfMin extends Command {
    private final CollectionVault collectionVault;

    public AddIfMin(CollectionVault collectionVault) {
        super(Commands.ADD_IF_MIN.getName(), Commands.ADD_IF_MIN.getDescription());
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AddIfMinRequest) request;
            double minHeight = minHeight();
            if (req.getPerson().getHeight() > minHeight) {
                if (!req.getPerson().verificate()) {
                    return new AddIfMinResponse(-1, false, "Человек не добавлен, так как поля не валидны!");
                } else {
                    long nextId = collectionVault.freeIds();
                    collectionVault.addToCollection(nextId, req.getPerson());
                    return new AddIfMinResponse(nextId, true, null);
                }
            } else {
                return new AddIfMinResponse(-1, false, "Человек не добавлен, так как его рост не является максимальным");
            }
        } catch (Exception e) {
            return new AddIfMinResponse(-1, false, e.toString());
        }
    }

    public double minHeight() {
        return collectionVault.getCollection().stream().map(Person::getHeight).mapToDouble(Double::doubleValue).min().orElse(-1);
    }
}