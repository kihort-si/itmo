package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class Tackle extends PhysicalMove{
    public Tackle() {
        super(Type.NORMAL, 35, 100);
    }

    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
    }

    @Override
    protected String describe() {
        return "применил Tackle";
    }
}