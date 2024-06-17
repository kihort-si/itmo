package ru.itmo.client.controllers;

/**
 * @author Nikita Vasilev
 */
public interface Controller {
    /**
     * @return transaction status
     */
    int execute();
}
