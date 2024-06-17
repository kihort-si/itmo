package ru.itmo.programming.server.vaults;

import ru.itmo.programming.common.collection.Location;
import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.utils.PersonComparator;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nikita Vasilev
 */
public class CollectionVault {
    private LinkedHashSet<Person> collection = new LinkedHashSet<>();
    private final ZonedDateTime initializationDate;

    public CollectionVault() {
        this.initializationDate = ZonedDateTime.now();
    }

    /**
     * @return Current collection of people.
     */
    public LinkedHashSet<Person> getCollection() {
        return collection;
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
     * @return
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
        /*for (Person person : collection) {
            if (person.getId() == id) {
                return person;
            }
        }
        return null;*/
    }

    /**
     * @param id The ID whose presence is to be checked in the collection.
     * @return A boolean value equal to finding a person with the given ID in the collection.
     */
    public boolean existId(long id) {
        return getById(id) != null;
    }

    /**
     * @param id A person's ID that needs to be set up for him or her.
     * @param person A person to be added to the collection.
     */
    public void addToCollection(long id, Person person) {
        collection.add(person);
        person.setId(id);
    }

    /**
     * @param id The ID of the person to be removed from the collection.
     */
    public void removeFromCollection(long id) {
        collection.removeIf(person -> person.getId() == id);
    }

    /**
     *Collection Cleanup.
     */
    public void clearCollection() {
        collection.clear();
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
