package ru.ifmo.se.pokemons;
import ru.ifmo.se.attacks.*;
import ru.ifmo.se.pokemon.*;

public class Pancham extends Pokemon {
    public Pancham (String name, int level) {
        super(name, level);
        setStats(67, 82, 62, 46, 48, 43);
        setType(Type.FIGHTING);
        setMove(new Psychic(), new DoubleTeam(), new RazorLeaf(), new MagicalLeaf());
    }
}
