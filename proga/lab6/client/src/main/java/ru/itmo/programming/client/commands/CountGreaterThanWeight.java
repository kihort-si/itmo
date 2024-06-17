package ru.itmo.programming.client.commands;

import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.CountGreaterThanWeightRequest;
import ru.itmo.programming.common.network.response.CountGreaterThanWeightResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class CountGreaterThanWeight extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public CountGreaterThanWeight(Console console, ClientManager clientManager) {
        super(Commands.COUNT_GREATER_THAN_WEIGHT.getName(), Commands.COUNT_GREATER_THAN_WEIGHT.getDescription());
        this.console = console;
        this.clientManager = clientManager;
    }

    @Override
    public boolean validateArgs(String[] args) {
        return args.length == 1;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (!validateArgs(args)) {
                console.printError("У команды " + getName() + " должен быть аргумент.");
                console.printError("Введите рост, людей меньше которого нужно вывести в консоль.");
            } else {
                double weight = Double.parseDouble(args[0]);
                var response = (CountGreaterThanWeightResponse) clientManager.sendAndReceiveCommand(new CountGreaterThanWeightRequest(weight));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println("Удалено " + response.getCount() + " человек.");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}
