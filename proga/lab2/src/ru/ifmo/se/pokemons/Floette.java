package ru.ifmo.se.pokemons;
import ru.ifmo.se.attacks.*;
import ru.ifmo.se.pokemon.*;

public class Floette extends Pokemon {
    public Floette(String name, int level) {
        super(name, level);
        setStats(54,45,47,75,98,52);
        setType(Type.FAIRY);
        setMove(new Psychic(), new DoubleTeam(), new RazorLeaf());
    }
}
