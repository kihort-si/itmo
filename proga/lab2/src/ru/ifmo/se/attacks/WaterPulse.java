package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class WaterPulse extends SpecialMove{
    public WaterPulse() {
        super(Type.WATER, 20, 100);
    }

    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
    }

    protected boolean isEffect = false;

    @Override
    protected void applyOppEffects(Pokemon p){
        if(Math.random() <= 0.2){
            Effect.confuse(p);
            isEffect = true;
        }
    }

    @Override
    protected String describe() {
        if (isEffect) {
            return "использовал WaterPulse";
        }
        else {
            return "использовал WaterPulse и сбил цель с толку";
        }
    }
}