package ru.itmo.is.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.dto.response.FileResponseDto;
import ru.itmo.is.model.*;
import ru.itmo.is.repository.MovieRepository;
import ru.itmo.is.repository.PersonRepository;
import ru.itmo.is.repository.UploadFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class UploadFileService {

    @Inject
    private UploadFileRepository uploadFileRepository;

    @Inject
    private FileHistoryService fileHistoryService;

    @Inject
    private PersonRepository personRepository;

    @Inject
    private MovieRepository movieRepository;

    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);

    public void processJsonFile(InputStream is, String fileName, Long fileSize) {
        File file = new File();
        file.setFilename(fileName);
        file.setSize(fileSize);
        file.setSuccess(false);
        file.setCreationDate(LocalDateTime.now());
        file.setObjectsCount(0);

        try {
            JsonNode rootNode = objectMapper.readTree(is);

                if (rootNode.has("persons")) {
                    JsonNode personsNode = rootNode.get("persons");
                    Iterator<Map.Entry<String, JsonNode>> fields = personsNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String personName = entry.getKey();
                        JsonNode personJson = entry.getValue();

                        Person person = new Person();
                        person.setName(personName);

                        try {
                            if (personJson.has("eyeColor")) {
                                String e = personJson.get("eyeColor").asText();
                                try {
                                    Color.valueOf(e);
                                    person.setEyeColor(Color.valueOf(e));
                                } catch (Exception ex) {
                                    throw new IllegalArgumentException("Недопустимый цвет глаз: " + e);
                                }
                            }

                            if (personJson.has("hairColor")) {
                                String h = personJson.get("hairColor").asText();
                                try {
                                    Color.valueOf(h);
                                    person.setHairColor(Color.valueOf(h));
                                } catch (Exception ex) {
                                    throw new IllegalArgumentException("Недопустимый цвет волос: " + h);
                                }
                            }

                            if (personJson.has("nationality")) {
                                String n = personJson.get("nationality").asText();
                                try {
                                    Country.valueOf(n);
                                    person.setNationality(Country.valueOf(n));
                                } catch (Exception ex) {
                                    throw new IllegalArgumentException("Недопустимая страна: " + n);
                                }
                            }

                            if (!personJson.has("location")) {
                                throw new IllegalArgumentException("Локация должна быть указана");
                            }

                            JsonNode locNode = personJson.get("location");

                            if (locNode == null || locNode.isNull()) {
                                throw new IllegalArgumentException("Локация не может быть null");
                            }

                            if (!locNode.isObject()) {
                                throw new IllegalArgumentException("Локация должна быть объектом");
                            }

                            if (!locNode.has("x") || locNode.get("x").isNull()) {
                                throw new IllegalArgumentException("Координата x локации должна быть указана");
                            }

                            if (!locNode.has("y") || locNode.get("y").isNull()) {
                                throw new IllegalArgumentException("Координата y локации должна быть указана");
                            }

                            if (!locNode.has("z") || locNode.get("z").isNull()) {
                                throw new IllegalArgumentException("Координата z локации должна быть указана");
                            }

                            if (!locNode.has("name") || locNode.get("name").isNull()) {
                                throw new IllegalArgumentException("Имя локации должно быть указано");
                            }

                            person.setLocation(objectMapper.convertValue(locNode, Location.class));

                            if (personJson.has("passportID") && !personJson.get("passportID").isNull()) {
                                String passportID = personJson.get("passportID").asText();

                                Person existingPerson = personRepository.findByPassportID(passportID);

                                if (existingPerson != null) {
                                    throw new IllegalArgumentException("Человек с PassportID '" + passportID + "' уже существует");
                                }

                                person.setPassportID(passportID);
                            } else {
                                throw new IllegalArgumentException("PassportID должен быть указан");
                            }

                            Person personWithSameNameAndLocation = personRepository.findByNameAndLocation(
                                    person.getName(), person.getLocation());

                            if (personWithSameNameAndLocation != null) {
                                throw new IllegalArgumentException("Человек с именем '" + person.getName() +
                                        "' и такой локацией уже существует");
                            }
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }

                        personRepository.save(person);
                        file.setObjectsCount(file.getObjectsCount() + 1);
                    }
                }

                if (rootNode.has("movies")) {
                    JsonNode moviesNode = rootNode.get("movies");
                    Iterator<Map.Entry<String, JsonNode>> moviesFields = moviesNode.fields();
                    while (moviesFields.hasNext()) {
                        Map.Entry<String, JsonNode> movieEntry = moviesFields.next();
                        String movieName = movieEntry.getKey();
                        JsonNode movieJson = movieEntry.getValue();

                        Movie movie = new Movie();
                        movie.setName(movieName);
                        movie.setCreationDate(java.time.LocalDateTime.now());

                        try {
                            if (!movieJson.has("coordinates")) {
                                throw new IllegalArgumentException("Координаты должны быть указаны");
                            }

                            JsonNode coordsNode = movieJson.get("coordinates");

                            if (coordsNode == null || coordsNode.isNull()) {
                                throw new IllegalArgumentException("Координаты не могут быть null");
                            }

                            if (!coordsNode.isObject()) {
                                throw new IllegalArgumentException("Координаты должны быть объектом");
                            }

                            if (!coordsNode.has("x") || coordsNode.get("x").isNull()) {
                                throw new IllegalArgumentException("Координата x должна быть указана");
                            }

                            if (!coordsNode.has("y") || coordsNode.get("y").isNull()) {
                                throw new IllegalArgumentException("Координата y должна быть указана");
                            }

                            double x = coordsNode.get("x").doubleValue();
                            if (x > 347) {
                                throw new IllegalArgumentException("Координата x должна быть меньше или равна 347");
                            }

                            movie.setCoordinates(objectMapper.convertValue(coordsNode, Coordinates.class));

                            if (movieJson.has("oscarsCount") && !movieJson.get("oscarsCount").isNull()) {
                                long oscarsCount = movieJson.get("oscarsCount").longValue();
                                if (oscarsCount <= 0) {
                                    throw new IllegalArgumentException("Количество Оскаров должно быть больше 0");
                                }
                                movie.setOscarsCount(oscarsCount);
                            } else {
                                throw new IllegalArgumentException("Количество Оскаров должно быть указано");
                            }

                            if (movieJson.has("budget")) {
                                Integer budget = movieJson.get("budget").isNull() ? null : movieJson.get("budget").intValue();
                                if (budget != null && budget <= 0) {
                                    throw new IllegalArgumentException("Бюджет должен быть больше 0");
                                }
                                movie.setBudget(budget);
                            }

                            if (movieJson.has("totalBoxOffice")) {
                                Long totalBoxOffice = movieJson.get("totalBoxOffice").isNull() ? null : movieJson.get("totalBoxOffice").longValue();
                                if (totalBoxOffice != null && totalBoxOffice <= 0) {
                                    throw new IllegalArgumentException("Общий сбор должен быть больше 0");
                                }
                                movie.setTotalBoxOffice(totalBoxOffice);
                            }

                            if (movieJson.has("length") && !movieJson.get("length").isNull()) {
                                int length = movieJson.get("length").intValue();
                                if (length <= 0) {
                                    throw new IllegalArgumentException("Длина фильма должна быть больше 0");
                                }
                                movie.setLength(length);
                            } else {
                                throw new IllegalArgumentException("Длина фильма должна быть указана");
                            }

                            if (movieJson.has("goldenPalmCount") && !movieJson.get("goldenPalmCount").isNull()) {
                                long goldenPalmCount = movieJson.get("goldenPalmCount").longValue();
                                if (goldenPalmCount <= 0) {
                                    throw new IllegalArgumentException("Количество Золотых пальмовых ветвей должно быть больше 0");
                                }
                                movie.setGoldenPalmCount(goldenPalmCount);
                            } else {
                                throw new IllegalArgumentException("Количество Золотых пальмовых ветвей должно быть указано");
                            }

                            if (movieJson.has("mpaaRating") && !movieJson.get("mpaaRating").isNull()) {
                                String r = movieJson.get("mpaaRating").asText();
                                try {
                                    MpaaRating.valueOf(r);
                                    movie.setMpaaRating(MpaaRating.valueOf(r));
                                } catch (Exception ex) {
                                    throw new IllegalArgumentException("Недопустимый рейтинг MPAA: " + r);
                                }
                            } else {
                                throw new IllegalArgumentException("Рейтинг MPAA должен быть указан");
                            }

                            if (movieJson.has("genre")) {
                                String g = movieJson.get("genre").asText();
                                try {
                                    MovieGenre.valueOf(g);
                                    movie.setGenre(MovieGenre.valueOf(g));
                                } catch (Exception ex) {
                                    throw new IllegalArgumentException("Недопустимый жанр фильма: " + g);
                                }
                            }

                            if (movieJson.has("director") && !movieJson.get("director").isNull()) {
                                movie.setDirector(resolvePersonRef(movieJson.get("director"), file));
                            } else {
                                throw new IllegalArgumentException("Режиссер должен быть указан");
                            }

                            movie.setScreenwriter(resolvePersonRef(movieJson.get("screenwriter"), file));
                            movie.setOperator(resolvePersonRef(movieJson.get("operator"), file));

                            Movie movieWithSameStaff = movieRepository.findByNameAndStaff(
                                    movie.getName(),
                                    movie.getDirector(),
                                    movie.getScreenwriter(),
                                    movie.getOperator());

                            if (movieWithSameStaff != null) {
                                throw new IllegalArgumentException("Фильм с названием '" + movie.getName() +
                                        "' и такой группой уже существует");
                            }
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }

                        movieRepository.save(movie);
                        file.setObjectsCount(file.getObjectsCount() + 1);
                    }
                }
                file.setSuccess(true);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            } finally {
                fileHistoryService.saveFileHistory(file);
            }
    }

    private Person resolvePersonRef(JsonNode node, File file) {
        if (node == null || node.isNull()) return null;

        try {
            if (node.isInt() || node.isLong()) {
                Long id = node.longValue();
                Person person = personRepository.findById(id);
                if (person == null) {
                    throw new IllegalArgumentException("Человек с ID " + id + " не найден");
                }
                return person;
            } else if (node.isObject()) {
                Person p = objectMapper.convertValue(node, Person.class);
                if (p.getPassportID() != null) {
                    Person existingPerson = personRepository.findByPassportID(p.getPassportID());

                    if (existingPerson != null) {
                        throw new IllegalArgumentException("Человек с PassportID '" + p.getPassportID() + "' уже существует");
                    }
                }

                Person personWithSameNameAndLocation = personRepository.findByNameAndLocation(
                        p.getName(), p.getLocation());

                if (personWithSameNameAndLocation != null) {
                    throw new IllegalArgumentException("Человек с именем '" + p.getName() +
                        "' и такой локацией уже существует");
                }
                file.setObjectsCount(file.getObjectsCount() + 1);
                return personRepository.save(p);
            } else {
                throw new IllegalArgumentException("Неправильная запись: " + node);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public List<FileResponseDto> getAllFiles() {
        return uploadFileRepository.getAllFiles().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private FileResponseDto convertToDTO(File file) {
        if (file == null) {
            return null;
        }

        FileResponseDto dto = new FileResponseDto();
        dto.setId(file.getId());
        dto.setFilename(file.getFilename());
        dto.setSize(file.getSize());
        dto.setSuccess(file.isSuccess());
        dto.setCreationDate(file.getCreationDate());
        dto.setObjectsCount(file.getObjectsCount());
        return dto;
    }
}
