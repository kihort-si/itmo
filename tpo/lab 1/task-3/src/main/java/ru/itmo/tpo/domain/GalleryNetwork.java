package ru.itmo.tpo.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryNetwork {
    private final List<GalleryPassage> passages = new ArrayList<>();

    public void addPassage(GalleryPassage passage) {
        if (passage == null) {
            throw new IllegalArgumentException("passage must not be null");
        }
        passages.add(passage);
    }

    public List<GalleryPassage> passages() {
        return Collections.unmodifiableList(passages);
    }
}
