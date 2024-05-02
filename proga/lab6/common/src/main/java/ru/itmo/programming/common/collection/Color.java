package ru.itmo.programming.common.collection;

import java.io.Serializable;

public enum Color implements Serializable {
    BLUE,
    YELLOW,
    ORANGE,
    WHITE;

    public static String getAsString() {
        StringBuilder names = new StringBuilder();
        Color[] colors = Color.values();
        for (int i = 0; i < colors.length; i++) {
            names.append(colors[i]);
            if (i < colors.length - 1) {
                names.append(", ");
            }
        }
        return names.toString();
    }
}