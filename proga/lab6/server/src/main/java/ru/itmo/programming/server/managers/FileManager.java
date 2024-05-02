package ru.itmo.programming.server.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ru.itmo.programming.common.collection.Person;
import ru.itmo.programming.common.exceptions.FileAccessRightsException;
import ru.itmo.programming.server.App;
import ru.itmo.programming.server.utils.ZonedDateTimeAdapter;
import ru.itmo.programming.server.vaults.CollectionVault;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Scanner;

public class FileManager {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .create();
    private final String name;
    private final CollectionVault collectionVault;

    public FileManager(String name, CollectionVault collectionVault) {
        this.name = name;
        this.collectionVault = collectionVault;
    }

    public void writeCollection(Collection<Person> collection) {
        try {
            File file = new File(name);
            if (!file.canWrite()) throw new FileAccessRightsException("нет прав доступа записи к файлу " + name);
            try (PrintWriter writer = new PrintWriter(new FileWriter(name))) {
                for (Person person : collection) {
                    String json = gson.toJson(person);
                    writer.println(json);
                }
            }
        } catch (FileAccessRightsException e) {
            App.logger.error(e.getMessage());
        } catch (IOException e) {
            App.logger.error("Не удалось записать данные: " + e.getMessage());
        }
    }

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
                                    collectionVault.addToCollection(person.getId(), person);
                                } else {
                                    App.logger.error("данные в строке не корректны: " + line);
                                    System.exit(1);
                                }
                            } catch (JsonSyntaxException e) {
                                App.logger.error("при парсинге строки: " + line);
                                App.logger.error(e.getMessage());
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    App.logger.error("Файл не найден");
                }
            }
        } catch (FileAccessRightsException e) {
            App.logger.error(e.getMessage());
        }
    }
}
