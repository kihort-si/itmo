package ru.itmo.programming.characters;

import ru.itmo.programming.actions.Beable;
import ru.itmo.programming.enums.Forms;

public class Woman extends Substance implements Beable {
    public Woman() {
        super("Женщина");
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
    public void be(String object, String description, Forms forms) {
        switch (forms) {
            case He:
                System.out.print(object + "был" + description);
                break;
            case She:
                System.out.print(object + "была" + description);
                break;
            case It:
                System.out.print(object + "было" + description);
                break;
            case They:
                System.out.print(object + "были" + description);
                break;
            default:
                System.out.println();
        }
    }
}
