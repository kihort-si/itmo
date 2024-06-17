package ru.itmo.server.vaults;

import org.slf4j.Logger;
import ru.itmo.common.collection.Location;
import ru.itmo.common.collection.Person;
import ru.itmo.common.utils.PersonComparator;
import ru.itmo.server.App;
import ru.itmo.server.database.DatabaseManger;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Nikita Vasilev
 */
public class CollectionVault {
    private final CopyOnWriteArrayList<Person> collection;
    private final ZonedDateTime initializationDate;

    public CollectionVault(DatabaseManger databaseManger) throws SQLException {
        this.initializationDate = ZonedDateTime.now();
        this.collection = databaseManger.readDatabase();

        if (!validateCollection()) {
            Logger logger = App.logger;
            logger.error("В базе данных хранятся недопустимые значения.");
            System.exit(2);
        }
    }

    /**
     * @return Current collection of people.
     */
    public CopyOnWriteArrayList
            <Person> getCollection() {
        return collection;
    }

    /**
     * @return boolean value, whether the values stored in the database are correct or not.
     */
    public boolean validateCollection() {
        for (Person person : collection) {
            return person.verificate();
        }
        return true;
    }

    /**
     * @return The date of the last initialization of the collection.
     */
    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * @return Type of collection.
     */
    public String getCollectionType() {
        return String.valueOf(Set.class);
    }

    /**
     * @return The type of elements that are in the collection.
     */
    public String getElementsType() {
        if (!collection.isEmpty()) {
            return String.valueOf(collection.iterator().next().getClass());
        } else {
            return null;
        }
    }

    /**
     * @return collection size in memory.
     */
    public int getCollectionSize() {
        return collection.size();
    }

    /**
     * @return The minimum free ID that can be assigned to a person in the collection.
     */
    public long freeIds() {
        Set<Long> usedIds = new HashSet<>();
        for (Person p : collection) {
            usedIds.add(p.getId());
        }

        long nextId = 1;
        while (usedIds.contains(nextId)) {
            nextId++;
        }
        return nextId;
    }

    /**
     * @param id The ID of the person to be retrieved.
     * @return The person with the specified ID from the collection.
     */
    public Person getById(long id) {
        return collection.stream().filter(person -> person.getId() == id).findFirst().orElse(null);
    }

    /**
     * @param id The ID whose presence is to be checked in the collection.
     * @return A boolean value equal to finding a person with the given ID in the collection.
     */
    public boolean existId(long id) {
        return getById(id) != null;
    }

    /**
     * @param id     A person's ID that needs to be set up for him or her.
     * @param person A person to be added to the collection.
     */
    public void addToCollection(long id, Person person, int creator) {
        collection.add(person);
        person.setId(id);
        person.setCreator(creator);
    }

    /**
     * @param id The ID of the person to be removed from the collection.
     */
    public void removeFromCollection(long id, int creator) {
        collection.removeIf(person -> person.getId() == id && person.getCreator() == creator);
    }

    /**
     * Collection Cleanup.
     */
    public void clearCollection(int creator) {
        collection.removeIf(person -> person.getCreator() == creator);
    }


    /**
     * @return The person with the maximum location value.
     */
    public Person maxLocation() {
        Comparator<Location> locationComparator = Comparator.comparing(Location::getX)
                .thenComparing(Location::getY)
                .thenComparing(Location::getZ);

        return getCollection().stream()
                .max(Comparator.comparing(Person::getLocation, locationComparator))
                .orElseThrow(() -> new NoSuchElementException("No persons found in the collection"));
    }

    /**
     * @return A list sorted by name of the collection.
     */
    public List<Person> getSortedCollection() {
        return new ArrayList<>(collection).stream().sorted(new PersonComparator()).collect(Collectors.toList());
    }
}
