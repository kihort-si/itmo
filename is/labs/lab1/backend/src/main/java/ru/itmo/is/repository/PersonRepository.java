package ru.itmo.is.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import ru.itmo.is.model.Color;
import ru.itmo.is.model.Country;
import ru.itmo.is.model.Movie;
import ru.itmo.is.model.Person;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PersonRepository {

    @Inject
    Session session;

    public Person save(Person person) {
        UnitOfWork uow = session.acquireUnitOfWork();

        Person result;
        if (person.getId() == null) {
            result = (Person) uow.registerNewObject(person);
        } else {
            Person existingPerson = (Person) uow.readObject(Person.class,
                new ExpressionBuilder().get("id").equal(person.getId()));

            if (existingPerson != null) {
                existingPerson.setName(person.getName());
                existingPerson.setEyeColor(person.getEyeColor());
                existingPerson.setHairColor(person.getHairColor());
                existingPerson.setLocation(person.getLocation());
                existingPerson.setPassportID(person.getPassportID());
                existingPerson.setNationality(person.getNationality());

                result = existingPerson;
            } else {
                result = (Person) uow.registerNewObject(person);
            }
        }

        uow.commit();
        return result;
    }

    public void delete(Person person) {
        UnitOfWork uow = session.acquireUnitOfWork();
        uow.deleteObject(person);
        uow.commit();
    }

    public List<Person> getAllPeople(String sortBy, String sortDir, Map<String, String> filters) {
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression filterExpression = null;

        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value == null || value.isEmpty()) {
                    continue;
                }

                Expression currentExpression;

                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    Expression path = builder.get(parts[0]);
                    for (int i = 1; i < parts.length - 1; i++) {
                        path = path.get(parts[i]);
                    }
                    currentExpression = path.get(parts[parts.length - 1]).equal(value);
                } else {
                    if (key.equals("eyeColor") || key.equals("hairColor") || key.equals("nationality")) {
                        try {
                            if (key.equals("eyeColor")) {
                                currentExpression = builder.get(key).equal(Color.valueOf(value));
                            } else if (key.equals("hairColor")) {
                                currentExpression = builder.get(key).equal(Color.valueOf(value));
                            } else {
                                currentExpression = builder.get(key).equal(Country.valueOf(value));
                            }
                        } catch (IllegalArgumentException e) {
                            currentExpression = builder.get("id").equal(-1L);
                        }
                    } else {
                        currentExpression = builder.get(key).equal(value);
                    }
                }

                if (filterExpression == null) {
                    filterExpression = currentExpression;
                } else {
                    filterExpression = filterExpression.and(currentExpression);
                }
            }
        }

        if (sortBy == null || sortDir == null) {
            ReadAllQuery query = new ReadAllQuery(Person.class);
            if (filterExpression != null) {
                query.setSelectionCriteria(filterExpression);
            }
            return (List<Person>) session.executeQuery(query);
        }

        if (!sortBy.contains(".")) {
            ReadAllQuery query = new ReadAllQuery(Person.class);
            if (filterExpression != null) {
                query.setSelectionCriteria(filterExpression);
            }
            if ("asc".equalsIgnoreCase(sortDir)) {
                query.addOrdering(builder.get(sortBy).ascending());
            } else {
                query.addOrdering(builder.get(sortBy).descending());
            }
            return (List<Person>) session.executeQuery(query);
        }

        String[] parts = sortBy.split("\\.");
        String relationField = parts[0];

        ReadAllQuery nullQuery = new ReadAllQuery(Person.class);
        Expression nullCriteria = new ExpressionBuilder().get(relationField).isNull();
        if (filterExpression != null) {
            nullCriteria = nullCriteria.and(filterExpression);
        }
        nullQuery.setSelectionCriteria(nullCriteria);
        List<Person> nullResults = (List<Person>) session.executeQuery(nullQuery);

        ReadAllQuery nonNullQuery = new ReadAllQuery(Person.class);
        ExpressionBuilder nonNullBuilder = new ExpressionBuilder();
        Expression nonNullCriteria = nonNullBuilder.get(relationField).notNull();
        if (filterExpression != null) {
            nonNullCriteria = nonNullCriteria.and(filterExpression);
        }
        nonNullQuery.setSelectionCriteria(nonNullCriteria);

        Expression sortExpr = builder.get(relationField);
        for (int i = 1; i < parts.length; i++) {
            sortExpr = sortExpr.get(parts[i]);
        }

        if ("asc".equalsIgnoreCase(sortDir)) {
            nonNullQuery.addOrdering(sortExpr.ascending());
        } else {
            nonNullQuery.addOrdering(sortExpr.descending());
        }

        List<Person> nonNullResults = (List<Person>) session.executeQuery(nonNullQuery);

        List<Person> combinedResults = new java.util.ArrayList<>(nullResults.size() + nonNullResults.size());
        if ("asc".equalsIgnoreCase(sortDir)) {
            combinedResults.addAll(nullResults);
            combinedResults.addAll(nonNullResults);
        } else {
            combinedResults.addAll(nonNullResults);
            combinedResults.addAll(nullResults);
        }

        return combinedResults;
    }

    public Person findById(Long id) {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setReferenceClass(Person.class);
        ExpressionBuilder builder = new ExpressionBuilder();
        query.setSelectionCriteria(builder.get("id").equal(id));
        return (Person) session.executeQuery(query);
    }
}
