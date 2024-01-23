package ru.itmo.programming.characters;
import ru.itmo.programming.actions.*;
import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.enums.Tenses;
import ru.itmo.programming.exceptions.EventProcessingException;

public class Freken extends Character implements Makeable, Fallable, FrekenBehave {

    Heart heart;
    public Freken() {
        super("Фрекен Снорк", Forms.SHE);
        heart = new Heart();
        heart.setHeartName("сердце");
    }

    @Override
    public String toString() {
        return "Имя:" + getName();
    }

    @Override
    public void runTo(String object, String direction, Tenses tenses) {
        switch (tenses) {
            case FUTURE:
                System.out.print(object + "побегу" + direction);
                break;
            case PAST:
                System.out.print(object + "побежала" + direction);
                break;
            case PRESENT:
                System.out.print(object + "бегу" + direction);
                break;
            default:
                System.out.println();
        }
    }

    @Override
    public void comeTo(String condition, String toObject) throws EventProcessingException {
        if (!toObject.isEmpty()) {
            System.out.print(condition + " приблизилась " + toObject);
        } else {
            throw new EventProcessingException(getName(), " ни к чему не приблизилась");
        }
    }

    @Override
    public void make(String object, String something) {
        System.out.print(object + " сделала " + something);
    }

    @Override
    public void look(String description, String direction) {
        System.out.print(description + " оглядела " + direction);
    }

    @Override
    public void cover(String object, String byObject) {
        System.out.print(" прикрыла " + object + byObject);
    }

    @Override
    public void dream(String object, String about, String action) throws EventProcessingException {
        if (!about.isEmpty()) {
            System.out.print(object + " мечтала " + about + action);
        } else {
            throw new EventProcessingException(getName(), " ни о чём не мечтала");
        }
    }


    private class Heart {
        private String heartName;

        public String getHeartName() {
            return heartName + " " + Freken.this.getName();
        }

        public void setHeartName(String heartName) {
            this.heartName = heartName;
        }
    }

    @Override
    public void fall(String describe, String condition, boolean isMoral) {
        if (isMoral) {
            System.out.print(heart.getHeartName() + describe + " упало " + condition);
        } else {
            System.out.print(describe + " приросла " + condition);
        }
    }

    @Override
    public void makeThink(Type type, String about) throws EventProcessingException {
        if (!about.isEmpty()) {
            switch (type) {
                case PLAN:
                    System.out.print("подумала " + about);
                    break;
                case DONE:
                    System.out.print("сказала " + about);
                    break;
            }
        } else {
            throw new EventProcessingException(getName(), " ни о чем не подумала и ничего не сказала");
        }
    }

    @Override
    public void climb(String object, String place, String where) {
        System.out.print(object + " лазала " + place + where);
    }
}
