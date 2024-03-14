package ru.itmo.programming.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ru.itmo.programming.collections.Person;
import ru.itmo.programming.utils.Console;
import ru.itmo.programming.utils.ZonedDateTimeAdapter;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.util.Set;

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
     *
     * @param collection the current collection to be written to the file
     * @param name name of the file to which the collection should be written
     */
    public void writeCollection(Set<Person> collection, String name) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(name))) {
            for (Person person : collection) {
                String json = gson.toJson(person);
                writer.println(json);
            }
            console.println("Коллекция успешно сохранена!");
        } catch (IOException e) {
            console.printError("Не удалось записать данные: " + e.getMessage());
        }
    }

    /**
     *
     * @param collection the current collection to be read from the file
     * @param name name of the file from which the collection is to be read
     */
    public void readCollection(Set<Person> collection, String name) {
        File file = new File(name);
        if (file.length() != 0) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty()) {
                        try {
                            Person person = gson.fromJson(line, Person.class);
                            collection.add(person);
                        } catch (JsonSyntaxException e) {
                            console.printError("Ошибка при парсинге строки: " + line);
                            console.printError("Ошибка: " + e.getMessage());
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                console.printError("Файл не найден");
            }
        }
    }
}