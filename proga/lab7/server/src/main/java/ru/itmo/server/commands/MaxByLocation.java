package ru.itmo.server.commands;

import ru.itmo.common.collection.Location;
import ru.itmo.common.collection.Person;
import ru.itmo.common.exceptions.EmptyCollectionException;
import ru.itmo.common.network.request.Request;
import ru.itmo.common.network.response.MaxByLocationResponse;
import ru.itmo.common.network.response.Response;
import ru.itmo.common.utils.Commands;
import ru.itmo.server.database.UserManager;
import ru.itmo.server.vaults.CollectionVault;

import java.util.Comparator;
import java.util.Optional;


/**
 * @author Nikita Vasilev
 */
public class MaxByLocation extends Command {
    private final CollectionVault collectionVault;
    private final UserManager userManager;

    public MaxByLocation(CollectionVault collectionVault, UserManager userManager) {
        super(Commands.MAX_BY_LOCATION.getName(), Commands.MAX_BY_LOCATION.getDescription());
        this.collectionVault = collectionVault;
        this.userManager = userManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            if (collectionVault.getCollection().isEmpty()) {
                throw new EmptyCollectionException("Коллекция пуста.");
            } else if (!userManager.checkUser(userManager.getUser(UserManager.getCurrentUser()))) {
                return new MaxByLocationResponse(null, "Вы не вошли. Используйте команду Authorization");
            } else {
                return new MaxByLocationResponse(maxLocation(), null);
            }
        } catch (Exception e) {
            return new MaxByLocationResponse(null, e.toString());
        }
    }

    private Optional<Person> maxLocation() {
        Comparator<Location> locationComparator = Comparator.comparing(Location::getX)
                .thenComparing(Location::getY)
                .thenComparing(Location::getZ);
        Optional<Location> maxLocation = collectionVault.getCollection().stream()
                .map(Person::getLocation)
                .max(locationComparator);

        Optional<Person> personWithMaxLocation = collectionVault.getCollection().stream()
                .filter(person -> person.getLocation().equals(maxLocation.get()))
                .findFirst();
        return personWithMaxLocation;
    }
}
