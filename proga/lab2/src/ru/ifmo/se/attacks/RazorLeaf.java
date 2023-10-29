package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class RazorLeaf extends PhysicalMove {
    public RazorLeaf() {
        super(Type.GRASS, 25, 95 );
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
        return "атаковал при помощи Razor Leaf";
    }
}
