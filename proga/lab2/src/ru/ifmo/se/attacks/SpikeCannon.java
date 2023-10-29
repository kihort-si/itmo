package ru.ifmo.se.attacks;
import ru.ifmo.se.pokemon.*;
import java.util.Random;

public class SpikeCannon extends PhysicalMove{
    public SpikeCannon() {
        super(Type.NORMAL, 15, 100);
    }

    @Override
    protected double calcCriticalHit(Pokemon att, Pokemon def) {
        Random random = new Random();
        int randomNumber = random.nextInt(4) + 2;
        if (randomNumber == 2 || randomNumber == 3) {
            return 9.0;
        }
        else {
            return 3.0;
        }
    }

    @Override
    protected String describe() {
        return "использовал SpikeCannon";
    }
}