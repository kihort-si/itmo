package ru.itmo.programming.enums;

public enum Forms {
    HE("он"),
    SHE("она"),
    IT("оно"),
    THEY("они");

    private final String russian;

    Forms(String russian) {
        this.russian = russian;
    }

    public String getRussian() {
        return russian;
    }
}
