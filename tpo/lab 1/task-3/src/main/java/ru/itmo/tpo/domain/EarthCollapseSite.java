package ru.itmo.tpo.domain;

public class EarthCollapseSite {
    private final GalleryNetwork network;
    private final String cause;

    public EarthCollapseSite(GalleryNetwork network, String cause) {
        if (network == null) {
            throw new IllegalArgumentException("network must not be null");
        }
        if (cause == null || cause.isBlank()) {
            throw new IllegalArgumentException("cause must not be blank");
        }
        this.network = network;
        this.cause = cause;
    }

    public GalleryNetwork network() {
        return network;
    }

    public String cause() {
        return cause;
    }
}
