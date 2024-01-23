package ru.itmo.programming.actions;

import ru.itmo.programming.enums.Tenses;
import ru.itmo.programming.exceptions.EventProcessingException;

public interface FrekenBehave {
    void climb(String object, String place, String where);
    void comeTo(String condition, String toObject) throws EventProcessingException;
    void cover(String object, String byObject);
    void dream(String object, String about, String action) throws EventProcessingException;
    void look(String description, String direction);
    void runTo(String object, String direction, Tenses tenses);
}
