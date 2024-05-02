package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.network.request.FilterLessThanHeightRequest;
import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.FilterLessThanHeightResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.common.utils.PersonComparator;
import ru.itmo.programming.server.vaults.CollectionVault;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nikita Vasilev
 */
public class FilterLessThanHeight extends Command {
    private final CollectionVault collectionVault;
    public FilterLessThanHeight(CollectionVault collectionVault) {
        super("filter_less_than_height", "вывести элементы, значение поля height которых меньше заданного");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            var req = (FilterLessThanHeightRequest) request;
            return new FilterLessThanHeightResponse(filteredByHeight(req.getHeight()), null);
        } catch (Exception e) {
            return new FilterLessThanHeightResponse(null, e.toString());
        }
    }

    private List<Person> filteredByHeight(double height) {
        return collectionVault.getCollection().stream().filter(person -> (person.getHeight().equals(height))).sorted(new PersonComparator()).collect(Collectors.toList());
    }
}