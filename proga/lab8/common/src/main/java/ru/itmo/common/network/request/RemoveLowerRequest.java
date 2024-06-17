package ru.itmo.common.network.request;

import ru.itmo.common.utils.Commands;

public class RemoveLowerRequest extends Request {
    private final double height;
    public RemoveLowerRequest(double height) {
        super(Commands.REMOVE_LOWER.getName());
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
}
