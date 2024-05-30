package ru.itmo.server.commands;

import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.FilterLessThanHeightRequest;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.FilterLessThanHeightResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.common.utils.PersonComparator;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nikita Vasilev
 */
public class FilterLessThanHeight extends Command {
    private final CollectionVault collectionVault;
    private final UserManager userManager;
    public FilterLessThanHeight(CollectionVault collectionVault, UserManager userManager) {
        super(Commands.FILTER_LESS_THAN_HEIGHT.getName(), Commands.FILTER_LESS_THAN_HEIGHT.getDescription());
        this.collectionVault = collectionVault;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new FilterLessThanHeightResponse(null, "Вы не вошли. Используйте команду Authorization");
            } else {
                var req = (FilterLessThanHeightRequest) request;
                return new FilterLessThanHeightResponse(filteredByHeight(req.getHeight()), null);
            }
        } catch (Exception e) {
            return new FilterLessThanHeightResponse(null, e.toString());
        }
    }

    private List<Person> filteredByHeight(double height) {
        return collectionVault.getCollection().stream().filter(person -> (person.getHeight().equals(height))).sorted(new PersonComparator()).collect(Collectors.toList());
    }
}