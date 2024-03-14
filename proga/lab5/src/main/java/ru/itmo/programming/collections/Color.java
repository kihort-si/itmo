package ru.itmo.programming.collections;

/**
 * @author Nikita Vasilev
 */
public enum Color {
    BLUE,
    YELLOW,
    ORANGE,
    WHITE;

    /**
     * @return string representation of possible eye colors for collection elements
     */
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
