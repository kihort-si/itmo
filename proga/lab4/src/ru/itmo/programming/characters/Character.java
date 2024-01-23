package ru.itmo.programming.characters;

import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.exceptions.EventProcessingException;
import ru.itmo.programming.exceptions.InvalidCharacterException;

public abstract class Character {
    private final String name;
    private final Forms forms;

    public Character(String name, Forms forms) {
        this.name = name;
        this.forms = forms;

        if (name == null || forms == null) {
            throw new InvalidCharacterException("Имя или лицо персонажа не могут быть null", new NullPointerException());
        } else if (name.isEmpty()) {
            throw new InvalidCharacterException("Не введены имя или лицо персонажа.", new IllegalArgumentException());
        }
    }

    public String getName() {
        return name;
    }

    public String getForms() {
        return forms.getRussian();
    }

    private final String[] anatomy = {"лицо", "волосы", "глаза", "щеки", "губы", "голова", "плечи", "чёлку", "груди", "спина", "руки"};

    public String getAnatomy(int index) {
        try {
            return anatomy[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Части тела с таким индексом не существует");
            return "";
        }
    }

    public enum Type {
        PLAN, DONE
    }

    public abstract void makeThink(Type type, String about) throws EventProcessingException;

    @Override
    public int hashCode() {
        return super.hashCode() + this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
