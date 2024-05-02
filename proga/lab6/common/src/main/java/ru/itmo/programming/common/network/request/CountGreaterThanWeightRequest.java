package ru.itmo.programming.common.network.request;

import ru.itmo.programming.common.utils.Commands;

public class CountGreaterThanWeightRequest extends Request {
    private final double weight;
    public CountGreaterThanWeightRequest(double weight) {
        super(Commands.COUNT_GREATER_THAN_GREAT.getName());
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
