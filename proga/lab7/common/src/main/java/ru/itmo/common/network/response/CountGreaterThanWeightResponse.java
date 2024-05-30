package ru.itmo.common.network.response;

import ru.itmo.common.utils.Commands;

public class CountGreaterThanWeightResponse extends Response {
    private final int count;
    public CountGreaterThanWeightResponse(Integer count, String error) {
        super(Commands.COUNT_GREATER_THAN_WEIGHT.getName(), error);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
