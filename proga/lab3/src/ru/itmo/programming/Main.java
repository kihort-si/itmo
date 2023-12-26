package ru.itmo.programming;
import ru.itmo.programming.characters.*;
import ru.itmo.programming.characters.Character;
import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.enums.Tenses;

public class Main {
    public static void main(String[] args) {
        Freken freken = new Freken();
        Woman woman = new Woman();

        freken.runTo("Сейчас же ", " за остальными --", Tenses.FUTURE);
        freken.makeThink(Character.Type.PLAN, freken.getForms());
        freken.runTo(", но не ", ".", Tenses.PAST);
        freken.makeThink(Character.Type.DONE, freken.getForms() + " себе 'Смелей! Давай посмотрим, кто это!' ");
        freken.comeTo("И вся дрожа,", "к тому, что лежало в воде. ");
        woman.be("Это", woman.getName() + ". ", Forms.SHE);
        freken.make(freken.getName(), "несколько робких шагов и ");
        freken.increment("словно к земле", "от изумления: ");
        woman.be(woman.getName(), "деревянная! ", Forms.SHE);
        woman.be(woman.getForms(), "ещё и удивительно красивая. ", Forms.SHE);
        woman.smile(woman.getAnatomy(0), " её безмятежно" );
        woman.be("у неё", "румяные " + woman.getAnatomy(3) + " и " + woman.getAnatomy(4) + ", круглые, широко раскрытые голубые " + woman.getAnatomy(2), Forms.THEY);
        woman.fall(" " + woman.getAnatomy(1), " ее, тоже голубые, длинными крашеными локонами", woman.getAnatomy(6));
    }
}
