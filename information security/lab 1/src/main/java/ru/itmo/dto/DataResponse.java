package ru.itmo.dto;

import java.util.List;

public record DataResponse(String requestedBy, List<DataItem> items) {
    public DataResponse {
        items = List.copyOf(items);
    }

    @Override
    public List<DataItem> items() {
        return List.copyOf(items);
    }
}
