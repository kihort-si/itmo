package ru.itmo.programming.enums;

public enum Nature {
    Rocks("скалам"),
    Cape("мыса"),
    Water("воде"),
    Lilies("лилий"),
    Coast("побережье"),
    Flowers("цветами");

    private String russian;

    Nature(String russian) {
        this.russian = russian;
    }

    public String getRussian() {
        return russian;
    }
}
