package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.network.request.CountGreaterThanWeightRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.CountGreaterThanWeightResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class CountGreaterThanWeight extends Command {
    private final CollectionVault collectionVault;

    public CountGreaterThanWeight(CollectionVault collectionVault) {
        super(Commands.COUNT_GREATER_THAN_WEIGHT.getName(), Commands.COUNT_GREATER_THAN_WEIGHT.getDescription());
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (CountGreaterThanWeightRequest) request;
            return new CountGreaterThanWeightResponse(counter(req.getWeight()), null);
        } catch (Exception e) {
            return new CountGreaterThanWeightResponse(null, e.toString());
        }
    }

    private int counter(double weight) {
        int count = 0;
        for (Person person : collectionVault.getCollection()) {
            if (person.getWeight() > weight) count++;
        }
        return count;
    }
}
