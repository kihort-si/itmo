package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class FilterLessThanHeightRequest extends Request {
    private final double height;

    public FilterLessThanHeightRequest(double height) {
        super(Commands.FILTER_LESS_THAN_HEIGHT.getName());
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
}
