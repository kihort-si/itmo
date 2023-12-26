package ru.itmo.programming.characters;
import ru.itmo.programming.actions.*;
import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.enums.Tenses;

public class Freken extends Character implements Runable, Comeable, Makeable, Incrementable {

    public Freken() {
        super("Фрекен Снорк", Forms.SHE);
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
    public void comeTo(String condition, String toObject) {
        System.out.print(condition + " приблизилась " + toObject);
    }

    @Override
    public void make(String object, String something) {
        System.out.print(object + " сделала " + something);
    }

    @Override
    public void increment(String describe, String condition) {
        System.out.print(describe + " приросла " + condition);
    }

    @Override
    public void makeThink(Type type, String about) {
        switch (type) {
            case PLAN:
                System.out.print(" подумала " + about);
                break;
            case DONE:
                System.out.print(" сказала " + about);
                break;
            default:
                System.out.println();
        }
    }
}
