package ru.itmo.programming.characters;
import ru.itmo.programming.enums.Forms;

public abstract class Character {
    private final String name;
    private final Forms forms;
    public Character(String name, Forms forms) {
        this.name = name;
        this.forms = forms;
    }

    public String getName() {
        return name;
    }

    public String getForms() {
        return forms.getRussian();
    }

    public enum Type {
        PLAN, DONE
    }

    public abstract void makeThink(Type type, String about);

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
