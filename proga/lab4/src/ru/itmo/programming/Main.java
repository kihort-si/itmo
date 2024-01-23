package ru.itmo.programming;
import ru.itmo.programming.characters.*;
import ru.itmo.programming.characters.Character;
import ru.itmo.programming.enums.Forms;
import ru.itmo.programming.enums.Nature;
import ru.itmo.programming.enums.Tenses;
import ru.itmo.programming.exceptions.EventProcessingException;

public class Main {
    public static void main(String[] args) {
        Freken freken = new Freken();
        Woman woman = new Woman();

        freken.climb(freken.getName(), "по " + Nature.Rocks.getRussian(), " далёкого " + Nature.Cape.getRussian());

        freken.cover(freken.getAnatomy(7), " венком из морских " + Nature.Lilies.getRussian() + " ");

        try {
            freken.dream(freken.getForms(), "о такой находке, чтобы все ахнули. ", "А затем " + freken.getForms() + " отдаст ее Муми-троллю.");
        } catch (EventProcessingException e) {
            System.err.println(e.getMessage());
        }

        System.out.print(" (Разумеется, если находка не украшение.) ");

        freken.look("Вздыхая " + freken.getForms(), Nature.Coast.getRussian() + " ");

        freken.fall(" вдруг", "в " + freken.getAnatomy(8), true);

        System.out.print(" В конце " + Nature.Cape.getRussian() + " ");

        woman.unrecognize();

        freken.runTo("Сейчас же ", " за остальными --", Tenses.FUTURE);

        try {
            freken.makeThink(Character.Type.PLAN, freken.getForms());
        } catch (EventProcessingException e) {
            System.err.println(e.getMessage());
        }

        freken.runTo(", но не ", ".", Tenses.PAST);

        try {
            freken.makeThink(Character.Type.DONE, freken.getForms() + " себе 'Смелей! Давай посмотрим, кто это!' ");
        } catch (EventProcessingException e) {
            System.err.println(e.getMessage());
        }

        try {
            freken.comeTo("И вся дрожа,", "к тому, что лежало в воде. ");
        } catch (EventProcessingException e) {
            System.err.println(e.getMessage());
        }

        freken.make(freken.getName(), "несколько робких шагов и ");

        freken.fall("словно к земле", "от изумления: ", false);

        woman.be("Это", woman.getName() + ". ", Forms.SHE);

        woman.be(woman.getName(), "деревянная! ", Forms.SHE);

        woman.be(woman.getForms(), "ещё и удивительно красивая. ", Forms.SHE);

        woman.smile(woman.getAnatomy(0), " её безмятежно" );

        woman.be("у неё", "румяные " + woman.getAnatomy(3) + " и " + woman.getAnatomy(4) + ", круглые, широко раскрытые голубые " + woman.getAnatomy(2), Forms.THEY);

        woman.fall(" " + woman.getAnatomy(1) + " ее, тоже голубые, длинными крашеными локонами", woman.getAnatomy(6) + " ",  false);

        Woman queen = new Woman() {
            @Override
            public String getName() {
                return " королева";
            }
        };

        try {
            freken.makeThink(Character.Type.PLAN, freken.getName() + ": Это\" + queen.getName() + \".");
        } catch (EventProcessingException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        woman.be(woman.getAnatomy(10), "скрещены на " + woman.getAnatomy(8) +", блиставшей золотыми " + Nature.Flowers.getRussian(), Forms.THEY);

        Woman.Dress dress = new Woman.Dress("платье");

        dress.flow(" " + dress.getName(), "мягкими красными складками.");
        dress.be(" " + Forms.IT.getRussian(), "из крашеного дерева.", Forms.IT);

        woman.be("У " + woman.getName() + " не", "спины", Forms.IT);

        }
}
