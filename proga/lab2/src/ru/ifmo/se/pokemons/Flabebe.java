package ru.ifmo.se.pokemons;
import ru.ifmo.se.attacks.*;
import ru.ifmo.se.pokemon.*;

public class Flabebe extends Pokemon {
    public Flabebe(String name, int level) {
        super(name, level);
        setStats(44, 38, 39, 61, 79, 42);
        setType(Type.FAIRY);
        setMove(new Psychic(), new DoubleTeam());
    }
}
