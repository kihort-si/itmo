package ru.itmo.programming.client.commands;

import ru.itmo.programming.client.network.ClientManager;
import ru.itmo.programming.common.utils.Commands;
import ru.itmo.programming.common.utils.Console;
import ru.itmo.programming.common.exceptions.APIException;
import ru.itmo.programming.common.network.request.FilterLessThanHeightRequest;
import ru.itmo.programming.common.network.response.FilterLessThanHeightResponse;

import java.io.IOException;

/**
 * @author Nikita Vasilev
 */
public class FilterLessThanHeight extends Command {
    private final Console console;
    private final ClientManager clientManager;

    public FilterLessThanHeight(Console console, ClientManager clientManager) {
        super(Commands.FILTER_LESS_THAN_HEIGHT.getName(), Commands.FILTER_LESS_THAN_HEIGHT.getDescription());
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
                double height = Double.parseDouble(args[0]);
                var response = (FilterLessThanHeightResponse) clientManager.sendAndReceiveCommand(new FilterLessThanHeightRequest(height));
                if (response.getError() != null && !response.getError().isEmpty()) {
                    throw new APIException(response.getError());
                }
                console.println(response.getFiltredList() + "\n");
            }
        } catch (IOException | ClassNotFoundException e) {
            console.printError("при работе с сервером.");
        } catch (APIException e) {
            console.printError(e.getMessage());
        }
    }
}