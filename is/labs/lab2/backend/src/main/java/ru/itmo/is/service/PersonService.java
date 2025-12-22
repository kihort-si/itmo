package ru.itmo.is.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.dto.request.PersonRequestDto;
import ru.itmo.is.dto.response.LocationResponseDto;
import ru.itmo.is.dto.response.PersonResponseDto;
import ru.itmo.is.model.Location;
import ru.itmo.is.model.Movie;
import ru.itmo.is.model.Person;
import ru.itmo.is.repository.MovieRepository;
import ru.itmo.is.repository.PersonRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class PersonService {

    @Inject
    private PersonRepository personRepository;
    @Inject
    private MovieRepository movieRepository;
    @Inject
    private MovieService movieService;

    public List<PersonResponseDto> getAllPeople(String sortBy, String sortDir, Map<String, String> filters) {
        return personRepository.getAllPeople(sortBy, sortDir, filters).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PersonResponseDto getPersonById(Long id) {
        Person person = personRepository.findById(id);
        return person != null ? convertToDTO(person) : null;
    }

    public PersonResponseDto createPerson(PersonRequestDto personDTO) {
        Person existingPerson = personRepository.findByPassportID(personDTO.getPassportID());
        if (existingPerson != null) {
            throw new IllegalArgumentException("Человек с PassportID " + personDTO.getPassportID() + " уже существует");
        }

        Person person = convertToEntity(personDTO);
        Person personWithSameNameAndLocation = personRepository.findByNameAndLocation(
            person.getName(), person.getLocation()
        );
        if (personWithSameNameAndLocation != null) {
            throw new IllegalArgumentException("Человек с таким именем и локацией уже существует");
        }

        person = personRepository.save(person);
        return convertToDTO(person);
    }

    public PersonResponseDto updatePerson(Long id, PersonRequestDto personDTO) {
        Person existingPerson = personRepository.findById(id);
        if (existingPerson == null) {
            return null;
        }

        Person personWithSamePassport = personRepository.findByPassportID(personDTO.getPassportID());
        if (personWithSamePassport != null && !personWithSamePassport.getId().equals(id)) {
            throw new IllegalArgumentException("Человек с PassportID " + personDTO.getPassportID() + " уже существует");
        }

        Person updatedPerson = convertToEntity(personDTO);
        Person personWithSameNameAndLocation = personRepository.findByNameAndLocation(
            updatedPerson.getName(), updatedPerson.getLocation()
        );
        if (personWithSameNameAndLocation != null && !personWithSameNameAndLocation.getId().equals(id)) {
            throw new IllegalArgumentException("Человек с таким именем и локацией уже существует");
        }

        updatedPerson.setId(existingPerson.getId());
        updatedPerson = personRepository.save(updatedPerson);
        return convertToDTO(updatedPerson);
    }

    public boolean deletePerson(Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return false;
        }
        List<Movie> relatedMovies = movieRepository.findMoviesByPerson(id);
        for (Movie movie : relatedMovies) {
            movieService.deleteMovie(movie.getId());
        }
        personRepository.delete(person);
        return true;
    }

    public PersonResponseDto convertToDTO(Person person) {
        if (person == null) {
            return null;
        }

        PersonResponseDto dto = new PersonResponseDto();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setEyeColor(person.getEyeColor());
        dto.setHairColor(person.getHairColor());

        if (person.getLocation() != null) {
            LocationResponseDto locationDto = new LocationResponseDto();
            locationDto.setX(person.getLocation().getX());
            locationDto.setY(person.getLocation().getY());
            locationDto.setZ(person.getLocation().getZ());
            locationDto.setName(person.getLocation().getName());
            dto.setLocation(locationDto);
        }

        dto.setPassportID(person.getPassportID());
        dto.setNationality(person.getNationality());
        return dto;
    }

    public Person convertToEntity(PersonRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Person entity = new Person();
        entity.setName(dto.getName());
        entity.setEyeColor(dto.getEyeColor());
        entity.setHairColor(dto.getHairColor());

        if (dto.getLocation() != null) {
            Location location = new Location();
            location.setX(dto.getLocation().getX());
            location.setY(dto.getLocation().getY());
            location.setZ(dto.getLocation().getZ());
            location.setName(dto.getLocation().getName());
            entity.setLocation(location);
        }

        entity.setPassportID(dto.getPassportID());
        entity.setNationality(dto.getNationality());
        return entity;
    }
}
