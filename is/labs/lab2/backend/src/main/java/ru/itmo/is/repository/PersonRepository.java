package ru.itmo.is.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import ru.itmo.is.model.Color;
import ru.itmo.is.model.Country;
import ru.itmo.is.model.Location;
import ru.itmo.is.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PersonRepository {

    @Inject
    EntityManager entityManager;

    public Person save(Person person) {

        if (person.getPassportID() != null) {
            TypedQuery<Person> query = entityManager.createQuery(
                            "SELECT p FROM Person p WHERE p.passportID = :passportID", Person.class)
                    .setParameter("passportID", person.getPassportID());
            List<Person> results = query.getResultList();
            if (!results.isEmpty()) {
                Person found = results.get(0);
                if (person.getId() == null || !found.getId().equals(person.getId())) {
                    throw new IllegalArgumentException("Человек с таким passportID уже существует.");
                }
            }
        }

        if (person.getName() != null && person.getLocation() != null) {
            TypedQuery<Person> query = entityManager.createQuery(
                            "SELECT p FROM Person p WHERE p.name = :name " +
                                    "AND p.location.x = :x AND p.location.y = :y " +
                                    "AND p.location.z = :z AND p.location.name = :locName", Person.class)
                    .setParameter("name", person.getName())
                    .setParameter("x", person.getLocation().getX())
                    .setParameter("y", person.getLocation().getY())
                    .setParameter("z", person.getLocation().getZ())
                    .setParameter("locName", person.getLocation().getName());
            List<Person> results = query.getResultList();
            if (!results.isEmpty()) {
                Person found = results.get(0);
                if (person.getId() == null || !found.getId().equals(person.getId())) {
                    throw new IllegalArgumentException("Человек с таким именем и локацией уже существует.");
                }
            }
        }

        Person result;
        try {
            if (person.getId() == null) {
                entityManager.persist(person);
                result = person;
            } else {
                Person existingPerson = entityManager.find(Person.class, person.getId());
                if (existingPerson != null) {
                    existingPerson.setName(person.getName());
                    existingPerson.setEyeColor(person.getEyeColor());
                    existingPerson.setHairColor(person.getHairColor());
                    existingPerson.setLocation(person.getLocation());
                    existingPerson.setPassportID(person.getPassportID());
                    existingPerson.setNationality(person.getNationality());
                    result = existingPerson;
                } else {
                    entityManager.persist(person);
                    result = person;
                }
            }

            return result;

        } catch (jakarta.persistence.PersistenceException e) {
            String message = e.getMessage();
            Throwable cause = e.getCause();
            if (message != null && (message.contains("unique constraint") ||
                    message.contains("duplicate key") || message.contains("UNIQUE constraint"))) {
                throw new IllegalArgumentException("Нарушение уникального ограничения");
            }
            if (cause != null && (cause.getMessage() != null &&
                    (cause.getMessage().contains("unique constraint") ||
                            cause.getMessage().contains("duplicate key")))) {
                throw new IllegalArgumentException("Нарушение уникального ограничения");
            }
            throw e;
        }
    }

    public void delete(Person person) {
        Person managedPerson = entityManager.find(Person.class, person.getId());
        if (managedPerson != null) {
            entityManager.remove(managedPerson);
        }
    }

    public List<Person> getAllPeople(String sortBy, String sortDir, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> cq = cb.createQuery(Person.class);
        Root<Person> root = cq.from(Person.class);

        List<Predicate> predicates = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value == null || value.isEmpty()) {
                    continue;
                }

                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    Path<?> path = root.get(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        path = path.get(parts[i]);
                    }
                    predicates.add(cb.equal(path, value));
                } else {
                    if (key.equals("eyeColor") || key.equals("hairColor")) {
                        try {
                            Color enumValue = Color.valueOf(value);
                            predicates.add(cb.equal(root.get(key), enumValue));
                        } catch (IllegalArgumentException e) {
                            predicates.add(cb.equal(root.get("id"), -1L));
                        }
                    } else if (key.equals("nationality")) {
                        try {
                            Country enumValue = Country.valueOf(value);
                            predicates.add(cb.equal(root.get(key), enumValue));
                        } catch (IllegalArgumentException e) {
                            predicates.add(cb.equal(root.get("id"), -1L));
                        }
                    } else {
                        predicates.add(cb.equal(root.get(key), value));
                    }
                }
            }
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (sortBy != null && sortDir != null) {
            if (sortBy.contains(".")) {
                String[] parts = sortBy.split("\\.");
                Path<?> sortPath = root.get(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    sortPath = sortPath.get(parts[i]);
                }
                if ("asc".equalsIgnoreCase(sortDir)) {
                    cq.orderBy(cb.asc(sortPath));
                } else {
                    cq.orderBy(cb.desc(sortPath));
                }
            } else {
                if ("asc".equalsIgnoreCase(sortDir)) {
                    cq.orderBy(cb.asc(root.get(sortBy)));
                } else {
                    cq.orderBy(cb.desc(root.get(sortBy)));
                }
            }
        }

        TypedQuery<Person> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public Person findById(Long id) {
        return entityManager.find(Person.class, id);
    }

    public Person findByPassportID(String passportID) {
        if (passportID == null) return null;

        TypedQuery<Person> query = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.passportID = :passportID", Person.class);
        query.setParameter("passportID", passportID);
        List<Person> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Person findByNameAndLocation(String name, Location location) {
        if (name == null || location == null) return null;

        TypedQuery<Person> query = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.name = :name " +
                        "AND p.location.x = :x AND p.location.y = :y " +
                        "AND p.location.z = :z AND p.location.name = :locName", Person.class);
        query.setParameter("name", name);
        query.setParameter("x", location.getX());
        query.setParameter("y", location.getY());
        query.setParameter("z", location.getZ());
        query.setParameter("locName", location.getName());
        List<Person> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
