package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class FocusBlast extends SpecialMove {
    public FocusBlast() {
        super(Type.FIGHTING, 5, 70);
    }

    protected boolean isEffect = false;

    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
        if (Math.random() <= 0.1) {
            def.setMod(Stat.SPECIAL_DEFENSE, -1);
            isEffect = true;
        }
    }

    @Override
    protected String describe() {
        if (isEffect) {
            return "взорвал";
        }
        else {
            return "взорвал и снизил специальную защиту";
        }
    }
}
