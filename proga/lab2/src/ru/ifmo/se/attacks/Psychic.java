package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;

public class Psychic extends SpecialMove {
    public Psychic() {
        super(Type.PSYCHIC, 10, 100);
    }

    protected boolean isEffect = false;

    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        if (Math.random() <= 0.1) {
            def.setMod(Stat.SPECIAL_DEFENSE, 1);
            isEffect = true;
        }
    }

    @Override
    protected String describe() {
        if (isEffect) {
            return "психологически атаковал";
        }
        else {
            return "психологически атаковал и снизил специальную защиту";
        }
    }
}
