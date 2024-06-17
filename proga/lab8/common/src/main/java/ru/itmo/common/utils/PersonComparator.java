package ru.itmo.common.utils;

import ru.itmo.common.collection.Person;

import java.util.Comparator;

/**
 * @author Nikita Vasilev
 */
public class PersonComparator implements Comparator<Person> {
    @Override
    public int compare(Person o1, Person o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
