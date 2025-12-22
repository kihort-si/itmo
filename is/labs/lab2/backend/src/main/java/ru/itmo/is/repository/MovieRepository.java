package ru.itmo.is.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import ru.itmo.is.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class MovieRepository {

    @Inject
    private EntityManager entityManager;

    public Movie save(Movie movie) {
        Person director = movie.getDirector();
        Person screenwriter = movie.getScreenwriter();
        Person operator = movie.getOperator();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);

        Predicate namePred = cb.equal(root.get("name"), movie.getName());
        Predicate directorPred = cb.equal(root.get("director").get("id"),
                director != null ? director.getId() : null);

        Predicate screenwriterPred;
        if (screenwriter == null) {
            screenwriterPred = cb.isNull(root.get("screenwriter"));
        } else {
            screenwriterPred = cb.equal(root.get("screenwriter").get("id"), screenwriter.getId());
        }

        Predicate operatorPred;
        if (operator == null) {
            operatorPred = cb.isNull(root.get("operator"));
        } else {
            operatorPred = cb.equal(root.get("operator").get("id"), operator.getId());
        }

        Predicate combinedPred = cb.and(namePred, directorPred, screenwriterPred, operatorPred);
        cq.where(combinedPred);

        TypedQuery<Movie> query = entityManager.createQuery(cq)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        List<Movie> results = query.getResultList();

        if (!results.isEmpty()) {
            Movie found = results.get(0);
            if (movie.getId() < 0 || found.getId() != movie.getId()) {
                throw new IllegalArgumentException("Фильм с таким именем и составом съемочной группы уже существует.");
            }
        }

        Movie result;
        try {
            if (movie.getId() < 0) {
                movie.setCreationDate(LocalDateTime.now());
                entityManager.persist(movie);
                result = movie;
            } else {
                Movie existingMovie = entityManager.find(Movie.class, movie.getId());
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
                    existingMovie.setGoldenPalmCount(movie.getGoldenPalmCount());
                    existingMovie.setOscarsCount(movie.getOscarsCount());
                    existingMovie.setTotalBoxOffice(movie.getTotalBoxOffice());
                    result = existingMovie;
                } else {
                    entityManager.persist(movie);
                    result = movie;
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

    public void delete(Movie movie) {
            Movie managedMovie = entityManager.find(Movie.class, movie.getId());
            if (managedMovie != null) {
                entityManager.remove(managedMovie);
            }
    }

    public List<Movie> getAllMovies(String sortBy, String sortDir, Map<String, String> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);

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
                    if (key.equals("genre")) {
                        try {
                            MovieGenre enumValue = MovieGenre.valueOf(value);
                            predicates.add(cb.equal(root.get(key), enumValue));
                        } catch (IllegalArgumentException e) {
                            predicates.add(cb.equal(root.get("id"), -1));
                        }
                    } else if (key.equals("mpaaRating")) {
                        try {
                            MpaaRating enumValue = MpaaRating.valueOf(value);
                            predicates.add(cb.equal(root.get(key), enumValue));
                        } catch (Exception e) {
                            predicates.add(cb.equal(root.get("id"), -1));
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

        TypedQuery<Movie> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public Movie findById(Integer id) {
        return entityManager.find(Movie.class, id);
    }

    public List<Movie> findMoviesByPerson(Long personId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);

        Predicate directorPred = cb.equal(root.get("director").get("id"), personId);
        Predicate screenwriterPred = cb.equal(root.get("screenwriter").get("id"), personId);
        Predicate operatorPred = cb.equal(root.get("operator").get("id"), personId);

        cq.where(cb.or(directorPred, screenwriterPred, operatorPred));

        return entityManager.createQuery(cq).getResultList();
    }

    public boolean deleteOneByGenre(String genre) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
            Root<Movie> root = cq.from(Movie.class);
            cq.where(cb.equal(root.get("genre"), MovieGenre.valueOf(genre)));

            List<Movie> movies = entityManager.createQuery(cq).getResultList();

            if (!movies.isEmpty()) {
                int randomIndex = new java.util.Random().nextInt(movies.size());
                Movie randomMovie = movies.get(randomIndex);
                entityManager.remove(randomMovie);
                return true;
            }
            return false;
    }

    public long countGoldenPalmWinners() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);
        cq.where(cb.greaterThan(root.get("goldenPalmCount"), 0L));

        List<Movie> movies = entityManager.createQuery(cq).getResultList();

        return movies.stream()
                .mapToLong(Movie::getGoldenPalmCount)
                .sum();
    }

    public List<Person> getScreenwritersWithoutOscars() {
        TypedQuery<Movie> allMoviesQuery = entityManager.createQuery(
                "SELECT m FROM Movie m", Movie.class);
        List<Movie> allMovies = allMoviesQuery.getResultList();

        Map<Person, List<Movie>> moviesByScreenwriter = allMovies.stream()
                .filter(movie -> movie.getScreenwriter() != null)
                .collect(Collectors.groupingBy(Movie::getScreenwriter));

        return moviesByScreenwriter.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .allMatch(movie -> movie.getOscarsCount() == 0))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void redistributeOscars(String fromGenre, String toGenre) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            CriteriaQuery<Movie> fromCq = cb.createQuery(Movie.class);
            Root<Movie> fromRoot = fromCq.from(Movie.class);
            fromCq.where(cb.equal(fromRoot.get("genre"), MovieGenre.valueOf(fromGenre)));
            List<Movie> fromMovies = entityManager.createQuery(fromCq).getResultList();

            CriteriaQuery<Movie> toCq = cb.createQuery(Movie.class);
            Root<Movie> toRoot = toCq.from(Movie.class);
            toCq.where(cb.equal(toRoot.get("genre"), MovieGenre.valueOf(toGenre)));
            List<Movie> toMovies = entityManager.createQuery(toCq).getResultList();

            if (fromMovies.isEmpty() || toMovies.isEmpty()) {
                return;
            }

            long totalOscars = fromMovies.stream()
                    .mapToLong(Movie::getOscarsCount)
                    .sum();

            for (Movie movie : fromMovies) {
                Movie managedMovie = entityManager.find(Movie.class, movie.getId());
                if (managedMovie != null) {
                    managedMovie.setOscarsCount(0L);
                }
            }

            long oscarsPerMovie = totalOscars / toMovies.size();
            long remainder = totalOscars % toMovies.size();

            for (int i = 0; i < toMovies.size(); i++) {
                Movie movie = toMovies.get(i);
                Movie managedMovie = entityManager.find(Movie.class, movie.getId());
                if (managedMovie != null) {
                    long currentOscars = managedMovie.getOscarsCount();
                    long additionalOscars = oscarsPerMovie + (i < remainder ? 1 : 0);
                    managedMovie.setOscarsCount(currentOscars + additionalOscars);
                }
            }
    }

    public Movie findByNameAndStaff(String name, Person director, Person screenwriter, Person operator) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);

        Predicate namePred = cb.equal(root.get("name"), name);
        Predicate directorPred = cb.equal(root.get("director").get("id"), director.getId());

        Predicate expr = cb.and(namePred, directorPred);

        if (screenwriter == null) {
            expr = cb.and(expr, cb.isNull(root.get("screenwriter")));
        } else {
            expr = cb.and(expr, cb.equal(root.get("screenwriter").get("id"), screenwriter.getId()));
        }

        if (operator == null) {
            expr = cb.and(expr, cb.isNull(root.get("operator")));
        } else {
            expr = cb.and(expr, cb.equal(root.get("operator").get("id"), operator.getId()));
        }

        cq.where(expr);
        TypedQuery<Movie> query = entityManager.createQuery(cq);
        List<Movie> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
