package ru.ifmo.se.pokemons;
import ru.ifmo.se.attacks.*;
import ru.ifmo.se.pokemon.*;

public class Florges extends Pokemon {
    public Florges(String name, int level) {
        super(name, level);
        setStats(78, 65, 68, 112, 154, 75);
        setType(Type.FAIRY);
        setMove(new Psychic(), new DoubleTeam(), new RazorLeaf(), new MagicalLeaf());
    }
}
