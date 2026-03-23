package ru.itmo.tpo;

import java.time.Duration;
import ru.itmo.tpo.domain.AirState;
import ru.itmo.tpo.domain.Blockage;
import ru.itmo.tpo.domain.BlockageMaterial;
import ru.itmo.tpo.domain.Flashlight;
import ru.itmo.tpo.domain.GalleryNetwork;
import ru.itmo.tpo.domain.GalleryPassage;
import ru.itmo.tpo.domain.Marvin;
import ru.itmo.tpo.domain.Zaphod;

public class Main {
    public static void main(String[] args) {
        Blockage blockage = new Blockage(BlockageMaterial.RUBBLE, 30.0);
        GalleryPassage passage = new GalleryPassage("A-B", blockage, 0.8, 0.9);
        GalleryNetwork network = new GalleryNetwork();
        network.addPassage(passage);

        Zaphod zaphod = new Zaphod();
        Marvin marvin = new Marvin();

        passage.clearBy(zaphod, Duration.ofSeconds(2));
        passage.clearBy(marvin, Duration.ofSeconds(2));

        AirState air = new AirState(0.4, 0.4);
        air.intensifyFromDepths(0.2);
        double visibility = passage.visibility(new Flashlight(1.0), air);

        System.out.println("Passage state: " + passage.state());
        System.out.println("Remaining mass: " + passage.blockage().remainingMass());
        System.out.println("Visibility: " + visibility);
    }
}