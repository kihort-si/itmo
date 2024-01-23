package ru.itmo.programming.characters;

import ru.itmo.programming.actions.Beable;
import ru.itmo.programming.actions.Fallable;
import ru.itmo.programming.actions.Layable;
import ru.itmo.programming.actions.Smileable;
import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.enums.Nature;
import ru.itmo.programming.exceptions.EventProcessingException;
import java.util.Objects;

public class Woman extends Character implements Beable, Smileable, Fallable {
    public Woman() {
        super("Женщина", Forms.SHE);
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
    public void fall(String describe, String condition, boolean isMoral) {
        if (isMoral) {
            System.out.print("");
        } else {
            System.out.print(describe + " спадали на " + condition);
        }
    }

    public void unrecognize() {
        class Someone extends Character implements Layable {

            public Someone() {
                super("кто-то", Forms.HE);
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
                    throw new EventProcessingException(getName(), "Ни о чем не подумала и ничего не сказала");
                }
            }

            @Override
            public void lie(Nature nature, Forms forms) {
                switch (forms) {
                    case HE -> {System.out.print(Someone.super.getName() + " лежал ");
                    }
                    case SHE -> {System.out.print(Someone.super.getName() + " лежала ");
                    }
                    case IT -> {System.out.print(Someone.super.getName() + " лежало ");
                    }
                    case THEY -> {System.out.print(Someone.super.getName() + " лежали ");
                    }
                }

                if (Objects.requireNonNull(nature) == Nature.Water) {
                    System.out.print("в воде ");
                } else {
                    System.out.print(" там нельзя лежать");
                }
            }
        }

        Someone someone = new Someone();
        someone.lie(Nature.Water, Forms.HE);
    }

    public static class Dress implements Beable {
        private String name;

        public Dress(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void flow(String object, String byObject) {
            System.out.print(object + " струилось " + byObject);
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
    }
}