import ru.ifmo.se.pokemon.*;
import ru.ifmo.se.pokemons.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle();

        Corsola p1 = new Corsola("Радиоактивная свинья", 1);
        Flabebe p2 = new Flabebe("Бабочка", 1);
        Floette p3 = new Floette("Фея", 1);
        Florges p4 = new Florges("Цветочек", 1);
        Pancham p5 = new Pancham("Хитрый медведь", 1);
        Pangoro p6 = new Pangoro("Злой медведь", 1);

        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);

        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);

        b.go();
    }
}