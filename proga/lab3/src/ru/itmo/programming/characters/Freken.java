package ru.itmo.programming.characters;
import ru.itmo.programming.actions.*;
import ru.itmo.programming.enums.Tenses;

public class Freken extends Substance implements Runable, Thinkable, Tellable, Comeable, Makeable, Incrementable
{
    public Freken() {
        super("Фрекен Снорк");
    }

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
        return "Имя:" + getName();
    }

    public String preposition() {
        return "она";
    }
    @Override
    public void runTo(String object, String direction, Tenses tenses) {
        switch (tenses) {
            case Future:
                System.out.print(object + "побегу" + direction);
                break;
            case Past:
                System.out.print(object + "побежала" + direction);
                break;
            case Present:
                System.out.print(object + "бегу" + direction);
                break;
            default:
                System.out.println();
        }
    }

    @Override
    public void think(String object) {
        System.out.print("подумала " + object);
    }

    @Override
    public void comeTo(String condition, String toObject) {
        System.out.print(condition + "приблизилась" + toObject);
    }

    @Override
    public void tell(String object, String toObject, String phrase) {
        System.out.print(" " + "Сказала " + object + "" + toObject + "" + phrase);
    }

    @Override
    public void make(String object, String something) {
        System.out.print(object + "сделала" + something);
    }

    @Override
    public void increment(String describe, String condition) {
        System.out.print(describe + "приросла" + condition);
    }
}
