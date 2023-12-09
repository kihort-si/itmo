package ru.itmo.programming.actions;
import ru.itmo.programming.enums.Tenses;

public interface Runable {
    void runTo(String object, String direction, Tenses tenses);
}
