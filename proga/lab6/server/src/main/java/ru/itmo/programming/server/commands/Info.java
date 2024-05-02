package ru.itmo.programming.server.commands;

import ru.itmo.programming.common.network.request.Request;
import ru.itmo.programming.common.network.response.InfoResponse;
import ru.itmo.programming.common.network.response.Response;
import ru.itmo.programming.server.vaults.CollectionVault;

/**
 * @author Nikita Vasilev
 */
public class Info extends Command {
    private final CollectionVault collectionVault;
    public Info(CollectionVault collectionVault) {
        super("info", "вывести в стандартный поток вывода информацию о коллекции");
        this.collectionVault = collectionVault;
    }

    @Override
    public Response execute(Request request) {
        try {
            String message = "информация о текущей коллекции:\n" +
                    "Тип: " + collectionVault.getCollectionType() + "\n" +
                    "Класс коллекции: " + collectionVault.getElementsType() + "\n" +
                    "Время инициализации: " + collectionVault.getInitializationDate() + "\n" +
                    "Количество элементов: " + collectionVault.getCollectionSize() + "\n";
            return new InfoResponse(message, null);
        } catch (Exception e) {
            return new InfoResponse(null, e.toString());
        }
    }
}
