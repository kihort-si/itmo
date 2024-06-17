package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.network.request.RemoveLowerRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.RemoveLowerResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.server.vaults.CollectionVault;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nikita Vasilev
 */
public class RemoveLower extends Command {
    private final CollectionVault collectionVault;

    public RemoveLower(CollectionVault collectionVault) {
        super(Commands.REMOVE_LOWER.getName(), Commands.REMOVE_LOWER.getDescription());
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (RemoveLowerRequest) request;
            return new RemoveLowerResponse(remove(req.getHeight()), null);
        } catch (Exception e) {
            return new RemoveLowerResponse(null, e.getMessage());
        }
    }

    private int remove(double height) {
        Set<Long> lowerElements = new HashSet<>();
        for (Person person : collectionVault.getCollection()) {
            if (person.getHeight() < height) {
                lowerElements.add(person.getId());
            }
        }
        for (long id : lowerElements) {
            collectionVault.removeFromCollection(id);
        }
        return lowerElements.size();
    }
}
