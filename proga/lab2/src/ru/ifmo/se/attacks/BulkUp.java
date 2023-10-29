package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class BulkUp extends StatusMove {
    public BulkUp() {
        super(Type.FIGHTING, 20, 0);
    }

    @Override
    protected void	applySelfEffects(Pokemon p) {
        p.setMod(Stat.ATTACK, 1);
        p.setMod(Stat.DEFENSE, 1);
    }

    @Override
    protected String describe() {
        return "получил BulkUp";
    }
}