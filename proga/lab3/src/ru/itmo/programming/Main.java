package ru.itmo.programming;
import ru.itmo.programming.characters.*;
import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.enums.Tenses;
import ru.itmo.programming.environment.*;

public class Main {
    public static void main(String[] args) {
        Freken freken = new Freken();
        Woman woman = new Woman();
        Face face = new Face();

        freken.runTo("Сейчас же ", " за остальными -- ", Tenses.Future);
        freken.think(freken.preposition());
        freken.runTo(", но не ", "", Tenses.Past);
        freken.tell(freken.preposition(), " себе: ", "\"Давай посмотрим, кто это!\". ");
        freken.comeTo("Вся дрожа, ", " к тому, что лежало в воде. ");
        woman.be("Это ", " большая женщина... ", Forms.She);
        freken.make(freken.getName() + " ", " несколько робких шагов и ");
        freken.increment("словно к земле ", " от изумления: ");
        woman.be(woman.getName() + " ", " деревянная! ", Forms.She);
        woman.be("И ещё " + woman.preposition() + " ", " удивительно красивая. ", Forms.She);
        face.faceable(" её безмятежно ", "улыбалось, ", Head.Face);
        woman.be("у неё ", "", Forms.They);
        face.faceable(" румяные ", "", Head.Cheeks);
        face.faceable(" круглые, ", "", Head.Lips);
        face.faceable("широко раскрытые голубые ", "", Head.Eyes);
        face.faceable(" её ", "тоже голубые, ", Head.Hair);
        face.faceable("длинными крашенными ", " спадали на плечи...", Head.Curls);
    }
}
