package ru.itmo.programming.characters;

import ru.itmo.programming.actions.Faceable;
import ru.itmo.programming.environment.Head;

public class Face extends Substance implements Faceable {
    public Face() {
        super("Лицо");
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
        return getName();
    }

    @Override
    public void faceable(String describe, String action, Head head) {
        switch (head) {
            case Cheeks:
                System.out.print(describe + "щёки ");
                break;
            case Curls:
                System.out.print(describe + "локонами" + action);
                break;
            case Eyes:
                System.out.print(describe + "глаза. ");
                break;
            case Face:
                System.out.print("Лицо" + describe + "" + action);
                break;
            case Hair:
                System.out.print("Волосы" + describe + action);
                break;
            case Lips:
                System.out.print("губы" + describe);
                break;
            default:
                System.out.println();
        }
    }
}
