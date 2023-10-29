package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class KarateChop extends PhysicalMove {
    public KarateChop() {
        super(Type.FIGHTING, 25, 100);
    }

    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
    }

    @Override
    protected double calcCriticalHit(Pokemon att, Pokemon def) {
        return 3.0;
    }

    @Override
    protected String describe() {
        return "применил технику каратэ";
    }
}