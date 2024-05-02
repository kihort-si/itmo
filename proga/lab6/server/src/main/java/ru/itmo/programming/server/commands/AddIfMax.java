package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.network.request.AddIfMaxRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.AddIfMaxResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class AddIfMax extends Command {
    private final CollectionVault collectionVault;
    public AddIfMax(CollectionVault collectionVault) {
        super("add_if_max", "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (AddIfMaxRequest) request;
            double maxHeight = maxHeight();
            if (req.getPerson().getHeight() > maxHeight) {
                if (!req.getPerson().verificate()) {
                    return new AddIfMaxResponse(-1, false,"Человек не добавлен, так как поля не валидны!");
                } else {
                    long nextId = collectionVault.freeIds();
                    collectionVault.addToCollection(nextId, req.getPerson());
                    return new AddIfMaxResponse(nextId, true, null);
                }
            } else {
                return new AddIfMaxResponse(-1, false, "Человек не добавлен, так как его рост не является максимальным");
            }
        } catch (Exception e) {
            return new AddIfMaxResponse(-1, false, e.toString());
        }
    }

    public double maxHeight() {
        return collectionVault.getCollection().stream().map(Person::getHeight).mapToDouble(Double::doubleValue).max().orElse(-1);
    }
}
