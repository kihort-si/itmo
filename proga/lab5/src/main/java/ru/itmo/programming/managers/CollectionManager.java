package ru.itmo.programming.managers;

import ru.itmo.programming.collections.Person;

import java.time.ZonedDateTime;
import java.util.*;


/**
 * @author Nikita Vasilev
 */
public class CollectionManager {
    private final LinkedHashSet<Person> collection = new LinkedHashSet<Person>();
    private final ZonedDateTime initializationDate;

    public CollectionManager() {
        this.initializationDate = ZonedDateTime.now();
    }

    /**
     *
     * @return a collection with elements
     */
    public Set<Person> getCollection() {
        return collection;
    }

    /**
     * @return minimum free ID value available to create the next collection element
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
     * @param id id value of the element, information about which is to be obtained
     * @return if there is an element with the given id in the collection, information about it, otherwise nothing
     */
    public Person getElementById(long id) {
        for (Person person : collection) {
            if (person.getId() == id) {
                return person;
            }
        }
        return null;
    }

    /**
     * @return the maximum height value of the collection elements
     */
    public double getMaxHeight() {
        double maxHeight = Double.MIN_VALUE;

        for (Person person : collection) {
            if (person.getHeight() > maxHeight) {
                maxHeight = person.getHeight();
            }
        }
        return maxHeight;
    }

    /**
     * @return the minimum height value of the collection elements
     */
    public double getMinHeight() {
        double minHeight = Double.MAX_VALUE;

        for (Person person : collection) {
            if (person.getHeight() < minHeight) {
                minHeight = person.getHeight();
            }
        }
        return minHeight;
    }

    /**
     *
     * @return current collection type
     */
    public String getCollectionType() {
        return String.valueOf(Set.class);
    }

    /**
     *
     * @return type of elements in the current collection
     */
    public String getElementsType() {
        if (!collection.isEmpty()) {
            return String.valueOf(collection.iterator().next().getClass());
        } else {
            return null;
        }
    }

    /**
     *
     * @return current collection size
     */
    public int getCollectionSize() {
        return collection.size();
    }

    /**
     *
     * @return the initialization date of the current collection
     */
    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     *
     * @param element element to be added to the current collection
     */
    public void addElementToCollection(Person element) {
        collection.add(element);
    }

    /**
     * clears the current collection
     */
    public void clearCollection() {
        collection.clear();
    }

    /**
     * @return Id of the last created collection element
     */
    public long lastCreatedPerson() {
        Person lastCreatedPerson = null;
        for (Person person : collection) {
            lastCreatedPerson = person;
        }
        return lastCreatedPerson.getId();
    }

    /**
     *
     * @param id id of the item to be removed from the collection
     * @return boolean value of the operation to remove an element from the collection
     */
    public boolean removeById(long id) {
        Iterator<Person> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            if (person.getId() == id) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}
