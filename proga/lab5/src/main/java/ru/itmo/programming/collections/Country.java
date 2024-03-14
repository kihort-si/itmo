package ru.itmo.programming.collections;

/**
 * @author Nikita Vasilev
 */
public enum Country {
    FRANCE,
    THAILAND,
    SOUTH_KOREA;

    /**
     * @return string representation of possible countries for collection elements
     */
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
