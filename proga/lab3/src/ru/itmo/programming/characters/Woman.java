package ru.itmo.programming.characters;

import ru.itmo.programming.actions.Beable;
import ru.itmo.programming.actions.Fallable;
import ru.itmo.programming.actions.Smileable;
import ru.itmo.programming.enums.Forms;

public class Woman extends Character implements Beable, Smileable, Fallable {

    private final String[] anatomy = {"лицо", "волосы", "глаза", "щеки", "губы", "голова", "плечи"};

    public String getAnatomy(int index) {
        return anatomy[index];
    }

    public Woman() {
        super("Женщина", Forms.SHE);
    }

    @Override
    public void makeThink(Type type, String about) {
        switch (type) {
            case PLAN:
                System.out.print("подумала " + about);
                break;
            case DONE:
                System.out.print("сказала " + about);
                break;
        }
    }

    @Override
    public String toString() {
        return "Имя:" + getName();
    }

    @Override
    public void be(String object, String description, Forms forms) {
        switch (forms) {
            case HE:
                System.out.print(object + " был " + description);
                break;
            case SHE:
                System.out.print(object + " была " + description);
                break;
            case IT:
                System.out.print(object + " было " + description);
                break;
            case THEY:
                System.out.print(object + " были " + description);
                break;
        }
    }

    @Override
    public void smile(String object, String description) {
        System.out.print(object + description + " улыбалось ");
    }

    @Override
    public void fall(String object, String description, String subject) {
        System.out.print(object + description + " спадали на " + subject);
    }
}
