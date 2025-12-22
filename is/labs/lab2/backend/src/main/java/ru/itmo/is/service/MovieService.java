package ru.itmo.is.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.dto.request.MovieRequestDto;
import ru.itmo.is.dto.response.CoordinatesResponseDto;
import ru.itmo.is.dto.response.MovieResponseDto;
import ru.itmo.is.dto.response.PersonResponseDto;
import ru.itmo.is.model.Coordinates;
import ru.itmo.is.model.Movie;
import ru.itmo.is.model.Person;
import ru.itmo.is.repository.MovieRepository;
import ru.itmo.is.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class MovieService {

    @Inject
    private MovieRepository movieRepository;

    @Inject
    private PersonService personService;

    @Inject
    private PersonRepository personRepository;

    public List<MovieResponseDto> getAllMovies(String sortBy, String sortDir, Map<String, String> filters) {
        return movieRepository.getAllMovies(sortBy, sortDir, filters).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieResponseDto getMovieById(Integer id) {
        Movie movie = movieRepository.findById(id);
        return movie != null ? convertToDTO(movie) : null;
    }

    public MovieResponseDto createMovie(MovieRequestDto movieDTO) {
        Movie movie = convertToEntity(movieDTO);
        movie = movieRepository.save(movie);
        return convertToDTO(movie);
    }

    public MovieResponseDto updateMovie(Integer id, MovieRequestDto movieDTO) {
        Movie existingMovie = movieRepository.findById(id);
        if (existingMovie == null) {
            return null;
        }

        Movie updatedMovie = convertToEntity(movieDTO);
        updatedMovie.setId(existingMovie.getId());
        updatedMovie = movieRepository.save(updatedMovie);
        return convertToDTO(updatedMovie);
    }

    public boolean deleteMovie(Integer id) {
        Movie movie = movieRepository.findById(id);
        if (movie == null) {
            return false;
        }

        List<Person> peopleFromMovie = new ArrayList<>();
        if (movie.getDirector() != null) {
            peopleFromMovie.add(movie.getDirector());
        }
        if (movie.getScreenwriter() != null) {
            peopleFromMovie.add(movie.getScreenwriter());
        }
        if (movie.getOperator() != null) {
            peopleFromMovie.add(movie.getOperator());
        }

        movieRepository.delete(movie);

        for (Person person : peopleFromMovie) {
            deleteMoviesAndPeopleCascade(person.getId(), new java.util.HashSet<>(), new java.util.HashSet<>());
        }

        return true;
    }

    private void deleteMoviesAndPeopleCascade(Long personId, java.util.Set<Long> processedPeople, java.util.Set<Integer> processedMovies) {
        if (processedPeople.contains(personId)) {
            return;
        }

        processedPeople.add(personId);

        List<Movie> relatedMovies = movieRepository.findMoviesByPerson(personId);

        List<Person> allRelatedPeople = new ArrayList<>();

        for (Movie relatedMovie : relatedMovies) {
            if (!processedMovies.contains(relatedMovie.getId())) {
                processedMovies.add(relatedMovie.getId());

                if (relatedMovie.getDirector() != null && !processedPeople.contains(relatedMovie.getDirector().getId())) {
                    allRelatedPeople.add(relatedMovie.getDirector());
                }
                if (relatedMovie.getScreenwriter() != null && !processedPeople.contains(relatedMovie.getScreenwriter().getId())) {
                    allRelatedPeople.add(relatedMovie.getScreenwriter());
                }
                if (relatedMovie.getOperator() != null && !processedPeople.contains(relatedMovie.getOperator().getId())) {
                    allRelatedPeople.add(relatedMovie.getOperator());
                }

                movieRepository.delete(relatedMovie);
            }
        }

        for (Person relatedPerson : allRelatedPeople) {
            deleteMoviesAndPeopleCascade(relatedPerson.getId(), processedPeople, processedMovies);
        }

        Person person = personRepository.findById(personId);
        if (person != null) {
            personRepository.delete(person);
        }
    }

    public boolean deleteOneByGenre(String genre) {
        try {
            return movieRepository.deleteOneByGenre(genre);
        } catch (Exception e) {
            return false;
        }
    }

    public long countGoldenPalmWinners() {
        return movieRepository.countGoldenPalmWinners();
    }

    public List<MovieResponseDto> getMoviesWithGoldenPalmCountLessThan(long count) {
        return movieRepository.getAllMovies(null, null, null).stream()
                .filter(movie -> movie.getGoldenPalmCount() < count)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PersonResponseDto> getScreenwritersWithoutOscars() {
        List<Person> screenwriters = movieRepository.getScreenwritersWithoutOscars();
        return screenwriters.stream()
                .map(personService::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean redistributeOscars(String fromGenre, String toGenre) {
        try {
            movieRepository.redistributeOscars(fromGenre, toGenre);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private MovieResponseDto convertToDTO(Movie movie) {
        if (movie == null) {
            return null;
        }

        MovieResponseDto dto = new MovieResponseDto();
        dto.setId(movie.getId());
        dto.setName(movie.getName());

        if (movie.getCoordinates() != null) {
            CoordinatesResponseDto coordinatesDto = new CoordinatesResponseDto();
            coordinatesDto.setX(movie.getCoordinates().getX());
            coordinatesDto.setY(movie.getCoordinates().getY());
            dto.setCoordinates(coordinatesDto);
        }

        dto.setCreationDate(movie.getCreationDate());
        dto.setOscarsCount(movie.getOscarsCount());
        dto.setBudget(movie.getBudget());
        dto.setTotalBoxOffice(movie.getTotalBoxOffice());
        dto.setMpaaRating(movie.getMpaaRating());
        dto.setDirector(personService.convertToDTO(movie.getDirector()));
        dto.setScreenwriter(personService.convertToDTO(movie.getScreenwriter()));
        dto.setOperator(personService.convertToDTO(movie.getOperator()));
        dto.setLength(movie.getLength());
        dto.setGoldenPalmCount(movie.getGoldenPalmCount());
        dto.setGenre(movie.getGenre());
        return dto;
    }

    private Movie convertToEntity(MovieRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Movie movie = new Movie();
        movie.setName(dto.getName());
        movie.setCreationDate(java.time.LocalDateTime.now());

        if (dto.getCoordinates() != null) {
            Coordinates coordinates = new Coordinates();
            coordinates.setX(dto.getCoordinates().getX());
            coordinates.setY(dto.getCoordinates().getY());
            movie.setCoordinates(coordinates);
        }

        movie.setOscarsCount(dto.getOscarsCount());
        movie.setBudget(dto.getBudget());
        movie.setTotalBoxOffice(dto.getTotalBoxOffice());
        movie.setMpaaRating(dto.getMpaaRating());

        if (dto.getDirectorId() != null) {
            Person director = personRepository.findById(dto.getDirectorId());
            movie.setDirector(director);
        }

        if (dto.getScreenwriterId() != null) {
            Person screenwriter = personRepository.findById(dto.getScreenwriterId());
            movie.setScreenwriter(screenwriter);
        }

        if (dto.getOperatorId() != null) {
            Person operator = personRepository.findById(dto.getOperatorId());
            movie.setOperator(operator);
        }

        movie.setLength(dto.getLength());
        movie.setGoldenPalmCount(dto.getGoldenPalmCount());
        movie.setGenre(dto.getGenre());
        return movie;
    }
}
