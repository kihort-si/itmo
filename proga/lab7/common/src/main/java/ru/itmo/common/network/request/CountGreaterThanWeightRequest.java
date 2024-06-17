package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class CountGreaterThanWeightRequest extends Request {
    private final double weight;

    public CountGreaterThanWeightRequest(double weight) {
        super(Commands.COUNT_GREATER_THAN_WEIGHT.getName());
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
