package ru.itmo.is.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import ru.itmo.is.dto.response.FileResponseDto;
import ru.itmo.is.model.*;
import ru.itmo.is.repository.MovieRepository;
import ru.itmo.is.repository.PersonRepository;
import ru.itmo.is.repository.UploadFileRepository;
import ru.itmo.is.storage.MinioFileStorage;
import ru.itmo.is.transaction.DatabaseParticipant;
import ru.itmo.is.transaction.MinioParticipant;
import ru.itmo.is.transaction.TwoPhaseCommitCoordinator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UploadFileService {

    @Inject
    private UploadFileRepository uploadFileRepository;

    @Inject
    private FileHistoryService fileHistoryService;

    @Inject
    private PersonRepository personRepository;

    @Inject
    private MovieRepository movieRepository;

    @Inject
    private MinioFileStorage minioFileStorage;

    @Inject
    private TwoPhaseCommitCoordinator twoPhaseCommitCoordinator;

    @Inject
    private DatabaseParticipant databaseParticipant;

    @Inject
    private MinioParticipant minioParticipant;

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);

    @Transactional(rollbackOn = Exception.class)
    public void processJsonFile(InputStream is, String fileName, Long fileSize) throws Exception {
        byte[] fileContent = is.readAllBytes();
        
        String objectKey = UUID.randomUUID() + "_" + fileName;
        LocalDateTime creationDate = LocalDateTime.now();

        final Long[] fileIdHolder = new Long[1];

        try {
            saveFileWithTwoPhaseCommit(fileName, objectKey, fileSize, creationDate, fileContent, fileIdHolder);
            
            int objectsCount = importDataFromFile(fileName, fileContent);
            
            if (fileIdHolder[0] != null) {
                fileHistoryService.updateFileHistory(fileIdHolder[0], true, objectsCount);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getName();
                if (e.getCause() != null) {
                    errorMessage += ": " + e.getCause().getMessage();
                }
            }
            e.printStackTrace();
            
            if (fileIdHolder[0] != null) {
                try {
                    fileHistoryService.updateFileHistory(fileIdHolder[0], false, 0);
                } catch (Exception ex) {
                }
            }
            
            throw new Exception(errorMessage != null ? errorMessage : "Ошибка при обработке файла", e);
        }
    }

    private void saveFileWithTwoPhaseCommit(String fileName, String objectKey, Long fileSize, 
                                            LocalDateTime creationDate, byte[] fileContent, 
                                            Long[] fileIdHolder) throws Exception {
        minioParticipant.setSaveOperation(objectKey, fileContent);
        
        final String fn = fileName;
        final String ok = objectKey;
        final Long fs = fileSize;
        final LocalDateTime cd = creationDate;
        
        databaseParticipant.setOperation(() -> {
            Long fileId = fileHistoryService.createFileHistory(fn, ok, fs, cd);
            fileIdHolder[0] = fileId;
            return null;
        });

        List<ru.itmo.is.transaction.TwoPhaseCommitParticipant> participants = new ArrayList<>();
        participants.add(databaseParticipant);
        participants.add(minioParticipant);
        
        twoPhaseCommitCoordinator.execute(participants);
    }

    private int importDataFromFile(String fileName, byte[] fileContent) throws Exception {
        try {
            JsonNode rootNode = objectMapper.readTree(new ByteArrayInputStream(fileContent));

            int personsCount = 0;
            int moviesCount = 0;
            final int[] nestedPersonsCount = {0};

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
                        parsePerson(person, personJson);
                        personRepository.save(person);
                        personsCount++;
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Ошибка при парсинге человека '" + personName + "': " + e.getMessage(), e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Ошибка при сохранении человека '" + personName + "': " + e.getMessage(), e);
                    }
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
                        parseMovie(movie, movieJson, nestedPersonsCount);
                        movieRepository.save(movie);
                        moviesCount++;
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Ошибка при парсинге фильма '" + movieName + "': " + e.getMessage(), e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Ошибка при сохранении фильма '" + movieName + "': " + e.getMessage(), e);
                    }
                }
            }

            int totalObjects = personsCount + moviesCount + nestedPersonsCount[0];
            return totalObjects;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Ошибка при чтении файла: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void parsePerson(Person person, JsonNode personJson) {
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
    }

    private void parseMovie(Movie movie, JsonNode movieJson, int[] nestedPersonsCount) {
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
            movie.setDirector(resolvePersonRef(movieJson.get("director"), nestedPersonsCount));
        } else {
            throw new IllegalArgumentException("Режиссер должен быть указан");
        }

        movie.setScreenwriter(resolvePersonRef(movieJson.get("screenwriter"), nestedPersonsCount));
        movie.setOperator(resolvePersonRef(movieJson.get("operator"), nestedPersonsCount));

        Movie movieWithSameStaff = movieRepository.findByNameAndStaff(
                movie.getName(),
                movie.getDirector(),
                movie.getScreenwriter(),
                movie.getOperator());

        if (movieWithSameStaff != null) {
            throw new IllegalArgumentException("Фильм с названием '" + movie.getName() +
                    "' и такой группой уже существует");
        }
    }

    private Person resolvePersonRef(JsonNode node, int[] nestedPersonsCount) {
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
                Person p = new Person();
                
                if (node.has("name") && !node.get("name").isNull()) {
                    p.setName(node.get("name").asText());
                }
                
                if (node.has("eyeColor") && !node.get("eyeColor").isNull()) {
                    p.setEyeColor(Color.valueOf(node.get("eyeColor").asText()));
                }
                
                if (node.has("hairColor") && !node.get("hairColor").isNull()) {
                    p.setHairColor(Color.valueOf(node.get("hairColor").asText()));
                }
                
                if (node.has("location") && !node.get("location").isNull()) {
                    JsonNode locNode = node.get("location");
                    Location loc = new Location();
                    if (locNode.has("x")) loc.setX(locNode.get("x").doubleValue());
                    if (locNode.has("y")) loc.setY(locNode.get("y").doubleValue());
                    if (locNode.has("z")) loc.setZ((float) locNode.get("z").doubleValue());
                    if (locNode.has("name") && !locNode.get("name").isNull()) {
                        loc.setName(locNode.get("name").asText());
                    }
                    p.setLocation(loc);
                }
                
                if (node.has("passportID") && !node.get("passportID").isNull()) {
                    p.setPassportID(node.get("passportID").asText());
                }
                
                if (node.has("nationality") && !node.get("nationality").isNull()) {
                    p.setNationality(Country.valueOf(node.get("nationality").asText()));
                }
                
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
                nestedPersonsCount[0]++;
                Person savedPerson = personRepository.save(p);
                return savedPerson;
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

    public byte[] getFileContent(Long fileId) throws Exception {
        File file = uploadFileRepository.findById(fileId);
        if (file == null) {
            throw new IllegalArgumentException("File not found with id: " + fileId);
        }
        if (file.getObjectKey() == null || file.getObjectKey().isEmpty()) {
            throw new IllegalArgumentException("File object key is not set for file id: " + fileId);
        }
        try {
            return minioFileStorage.get(file.getObjectKey());
        } catch (Exception e) {
            throw new Exception("Failed to get file from MinIO: " + e.getMessage(), e);
        }
    }

    public File getFileById(Long fileId) {
        return uploadFileRepository.findById(fileId);
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
