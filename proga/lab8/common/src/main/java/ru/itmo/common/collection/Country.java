package ru.itmo.common.collection;

import java.io.Serializable;

public enum Country implements Serializable {
    FRANCE,
    THAILAND,
    SOUTH_KOREA;

    public static String getAsString() {
        StringBuilder names = new StringBuilder();
        Country[] country = Country.values();
        for (int i = 0; i < country.length; i++) {
            names.append(country[i]);
            if (i < country.length - 1) {
                names.append(", ");
            }
        }
        return names.toString();
    }
}
