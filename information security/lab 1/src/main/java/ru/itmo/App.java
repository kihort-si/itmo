package ru.itmo;

import ru.itmo.config.ApplicationFactory;
import ru.itmo.config.ApplicationSettings;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        ApplicationSettings settings = ApplicationSettings.fromEnvironment();
        ApplicationFactory.create(settings).start(settings.port());
    }
}
