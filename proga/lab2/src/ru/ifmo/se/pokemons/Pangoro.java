package ru.ifmo.se.pokemons;
import ru.ifmo.se.attacks.*;
import ru.ifmo.se.pokemon.*;

public class Pangoro extends Pokemon {
    public Pangoro(String name, int level) {
        super(name, level);
        setStats(95, 124, 78, 69, 71, 58);
        setType(Type.FIGHTING, Type.DARK);
        setMove(new BulkUp(), new Tackle(), new KarateChop(), new FocusBlast());
    }
}
