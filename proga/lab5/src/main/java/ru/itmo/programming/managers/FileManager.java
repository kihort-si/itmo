package ru.itmo.programming.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ru.itmo.programming.collections.Person;
import ru.itmo.programming.exceptions.FileAccessRightsException;
import ru.itmo.programming.utils.Console;
import ru.itmo.programming.utils.ZonedDateTimeAdapter;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Scanner;

/**
 * @author Nikita Vasilev
 */
public class FileManager {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .create();
    private final String name;
    private final CollectionManager collectionManager;
    private final Console console;


    public FileManager(String name, CollectionManager collectionManager, Console console) {
        this.name = name;
        this.collectionManager = collectionManager;
        this.console = console;
    }

    /**
     * Writes the collection to the json file specified in the environment variable
     * @throws FileAccessRightsException cannot write the file due to lack of access rights
     */
    public void writeCollection() {
        try {
            File file = new File(name);
            if (!file.canWrite()) throw new FileAccessRightsException("нет прав доступа записи к файлу " + name);
            try (PrintWriter writer = new PrintWriter(new FileWriter(name))) {
                for (Person person : collectionManager.getCollection()) {
                    String json = gson.toJson(person);
                    writer.println(json);
                }
                console.println("Коллекция успешно сохранена!");
            } catch (IOException e) {
                console.printError("Не удалось записать данные: " + e.getMessage());
            }
        } catch (FileAccessRightsException e) {
            console.printError(e.getMessage());
        }
    }

    /**
     * Reads a collection from the json file specified in the environment variable
     * @throws FileAccessRightsException cannot read the file due to lack of access rights
     * @throws JsonSyntaxException unable to read file due to incorrect json syntax
     */
    public void readCollection() {
        try {
            File file = new File(name);
            if (!file.canRead()) throw new FileAccessRightsException("нет прав доступа чтения к файлу " + name);
            if (file.length() != 0) {
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (!line.isEmpty()) {
                            try {
                                Person person = gson.fromJson(line, Person.class);
                                if (person.verificate()) {
                                    collectionManager.getCollection().add(person);
                                } else {
                                    console.printError("данные в строке не корректны: " + line);
                                    System.exit(1);
                                }
                            } catch (JsonSyntaxException e) {
                                console.printError("при парсинге строки: " + line);
                                console.printError(e.getMessage());
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    console.printError("Файл не найден");
                }
            }
        } catch (FileAccessRightsException e) {
            console.printError(e.getMessage());
        }
    }
}