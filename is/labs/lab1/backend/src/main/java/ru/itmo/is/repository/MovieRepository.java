package ru.itmo.is.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import ru.itmo.is.model.Movie;
import ru.itmo.is.model.MovieGenre;
import ru.itmo.is.model.MpaaRating;
import ru.itmo.is.model.Person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MovieRepository {

    @Inject
    private Session session;

    public Movie save(Movie movie) {
        UnitOfWork uow = session.acquireUnitOfWork();

        if (movie.getDirector() != null) {
            movie.setDirector((Person) uow.registerObject(movie.getDirector()));
        }
        if (movie.getScreenwriter() != null) {
            movie.setScreenwriter((Person) uow.registerObject(movie.getScreenwriter()));
        }
        if (movie.getOperator() != null) {
            movie.setOperator((Person) uow.registerObject(movie.getOperator()));
        }

        Movie result;
        if (movie.getId() < 0) {
            movie.setCreationDate(LocalDateTime.now());
            result = (Movie) uow.registerNewObject(movie);
        } else {
            Movie existingMovie = (Movie) uow.readObject(Movie.class,
                    new ExpressionBuilder().get("id").equal(movie.getId()));

            if (existingMovie != null) {
                existingMovie.setName(movie.getName());
                existingMovie.setBudget(movie.getBudget());
                existingMovie.setCoordinates(movie.getCoordinates());
                existingMovie.setGenre(movie.getGenre());
                existingMovie.setMpaaRating(movie.getMpaaRating());
                existingMovie.setScreenwriter(movie.getScreenwriter());
                existingMovie.setLength(movie.getLength());
                existingMovie.setDirector(movie.getDirector());
                existingMovie.setOperator(movie.getOperator());
                existingMovie.setCreationDate(movie.getCreationDate());
                existingMovie.setGoldenPalmCount(movie.getGoldenPalmCount());
                existingMovie.setOscarsCount(movie.getOscarsCount());
                existingMovie.setTotalBoxOffice(movie.getTotalBoxOffice());

                result = existingMovie;
            } else {
                result = (Movie) uow.registerNewObject(movie);
            }
        }

        uow.commit();

        return result;
    }

    public void delete(Movie movie) {
        UnitOfWork uow = session.acquireUnitOfWork();
        uow.deleteObject(movie);
        uow.commit();
    }

    public List<Movie> getAllMovies(String sortBy, String sortDir, Map<String, String> filters) {
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
                    if (key.equals("genre")) {
                        try {
                            MovieGenre enumValue = MovieGenre.valueOf(value);
                            currentExpression = builder.get(key).equal(enumValue);
                        } catch (IllegalArgumentException e) {
                            currentExpression = builder.get("id").equal(-1L);
                        }
                    } else if (key.equals("mpaaRating")) {
                        try {
                            MpaaRating enumValue = MpaaRating.valueOf(value);
                            currentExpression = builder.get(key).equal(enumValue);
                        } catch (Exception e) {
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
            ReadAllQuery query = new ReadAllQuery(Movie.class);
            if (filterExpression != null) {
                query.setSelectionCriteria(filterExpression);
            }
            return (List<Movie>) session.executeQuery(query);
        }

        if (!sortBy.contains(".")) {
            ReadAllQuery query = new ReadAllQuery(Movie.class);
            if (filterExpression != null) {
                query.setSelectionCriteria(filterExpression);
            }
            if ("asc".equalsIgnoreCase(sortDir)) {
                query.addOrdering(builder.get(sortBy).ascending());
            } else {
                query.addOrdering(builder.get(sortBy).descending());
            }
            return (List<Movie>) session.executeQuery(query);
        }

        String[] parts = sortBy.split("\\.");
        String relationField = parts[0];

        ReadAllQuery nullQuery = new ReadAllQuery(Movie.class);
        Expression nullCriteria = new ExpressionBuilder().get(relationField).isNull();
        if (filterExpression != null) {
            nullCriteria = nullCriteria.and(filterExpression);
        }
        nullQuery.setSelectionCriteria(nullCriteria);
        List<Movie> nullResults = (List<Movie>) session.executeQuery(nullQuery);

        ReadAllQuery nonNullQuery = new ReadAllQuery(Movie.class);
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

        List<Movie> nonNullResults = (List<Movie>) session.executeQuery(nonNullQuery);

        List<Movie> combinedResults = new java.util.ArrayList<>(nullResults.size() + nonNullResults.size());
        if ("asc".equalsIgnoreCase(sortDir)) {
            combinedResults.addAll(nullResults);
            combinedResults.addAll(nonNullResults);
        } else {
            combinedResults.addAll(nonNullResults);
            combinedResults.addAll(nullResults);
        }

        return combinedResults;
    }

    public Movie findById(Integer id) {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setReferenceClass(Movie.class);
        ExpressionBuilder builder = new ExpressionBuilder();
        query.setSelectionCriteria(builder.get("id").equal(id));
        return (Movie) session.executeQuery(query);
    }

    public List<Movie> findMoviesByPerson(Long personId) {
        ReadAllQuery query = new ReadAllQuery(Movie.class);
        ExpressionBuilder builder = new ExpressionBuilder();

        Expression criteria = builder.get("director").get("id").equal(personId)
                .or(builder.get("screenwriter").get("id").equal(personId))
                .or(builder.get("operator").get("id").equal(personId));

        query.setSelectionCriteria(criteria);
        return (List<Movie>) session.executeQuery(query);
    }

    public boolean deleteOneByGenre(String genre) {
        UnitOfWork uow = session.acquireUnitOfWork();
        ExpressionBuilder builder = new ExpressionBuilder();
        ReadAllQuery query = new ReadAllQuery(Movie.class);
        query.setSelectionCriteria(builder.get("genre").equal(MovieGenre.valueOf(genre)));
        List<Movie> movies = (List<Movie>) session.executeQuery(query);

        if (!movies.isEmpty()) {
            int randomIndex = new java.util.Random().nextInt(movies.size());
            Movie randomMovie = movies.get(randomIndex);
            uow.deleteObject(randomMovie);
            uow.commit();
            return true;
        }

        return false;
    }

    public long countGoldenPalmWinners() {
        ReadAllQuery query = new ReadAllQuery(Movie.class);
        ExpressionBuilder builder = new ExpressionBuilder();
        query.setSelectionCriteria(builder.get("goldenPalmCount").greaterThan(0));
        List<Movie> movies = (List<Movie>) session.executeQuery(query);

        return movies.stream()
                .mapToLong(Movie::getGoldenPalmCount)
                .sum();
    }

    public List<Person> getScreenwritersWithoutOscars() {
        ReadAllQuery allMoviesQuery = new ReadAllQuery(Movie.class);
        List<Movie> allMovies = (List<Movie>) session.executeQuery(allMoviesQuery);

        java.util.Map<Person, List<Movie>> moviesByScreenwriter = allMovies.stream()
                .filter(movie -> movie.getScreenwriter() != null)
                .collect(java.util.stream.Collectors.groupingBy(Movie::getScreenwriter));

        return moviesByScreenwriter.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .allMatch(movie -> movie.getOscarsCount() == 0))
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    public void redistributeOscars(String fromGenre, String toGenre) {
        UnitOfWork uow = session.acquireUnitOfWork();
        ExpressionBuilder builder = new ExpressionBuilder();

        ReadAllQuery fromQuery = new ReadAllQuery(Movie.class);
        fromQuery.setSelectionCriteria(builder.get("genre").equal(MovieGenre.valueOf(fromGenre)));
        List<Movie> fromMovies = (List<Movie>) session.executeQuery(fromQuery);

        ReadAllQuery toQuery = new ReadAllQuery(Movie.class);
        toQuery.setSelectionCriteria(new ExpressionBuilder().get("genre").equal(MovieGenre.valueOf(toGenre)));
        List<Movie> toMovies = (List<Movie>) session.executeQuery(toQuery);

        if (fromMovies.isEmpty() || toMovies.isEmpty()) {
            return;
        }

        long totalOscars = fromMovies.stream()
                .mapToLong(m -> m.getOscarsCount())
                .sum();

        for (Movie movie : fromMovies) {
            Movie managedMovie = (Movie) uow.readObject(Movie.class,
                    new ExpressionBuilder().get("id").equal(movie.getId()));
            managedMovie.setOscarsCount(0L);
        }

        long oscarsPerMovie = totalOscars / toMovies.size();
        long remainder = totalOscars % toMovies.size();

        for (int i = 0; i < toMovies.size(); i++) {
            Movie movie = toMovies.get(i);
            Movie managedMovie = (Movie) uow.readObject(Movie.class,
                    new ExpressionBuilder().get("id").equal(movie.getId()));
            long currentOscars = managedMovie.getOscarsCount();
            long additionalOscars = oscarsPerMovie + (i < remainder ? 1 : 0);
            managedMovie.setOscarsCount(currentOscars + additionalOscars);
        }

        uow.commit();
    }
}
