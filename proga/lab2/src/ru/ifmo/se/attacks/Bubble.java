package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class Bubble extends SpecialMove{
    public Bubble() {
        super(Type.WATER, 30, 100);
    }

    protected boolean isEffect = false;

    @Override
    protected void 	applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
        if (Math.random() < 0.1) {
            def.setMod(Stat.SPEED, -1);
            isEffect = true;
        }
    }

    @Override
    protected String describe() {
        if (isEffect) {
            return "применил Bubble";
        }
        else {
            return "применил Bubble и снизил скорость цели";
        }
    }
}