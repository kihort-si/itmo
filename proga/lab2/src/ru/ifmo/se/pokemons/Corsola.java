package ru.ifmo.se.pokemons;
import ru.ifmo.se.attacks.*;
import ru.ifmo.se.pokemon.*;

public class Corsola extends Pokemon {
    public Corsola (String name, int level) {
        super(name, level);
        setStats(65, 55, 95, 65, 95, 35);
        setType(Type.WATER, Type.ROCK);
        setMove(new SpikeCannon(), new WaterPulse(), new Bubble(), new Tackle());
    }
}
