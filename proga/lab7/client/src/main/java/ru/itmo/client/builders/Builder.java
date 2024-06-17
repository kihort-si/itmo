package ru.itmo.client.builders;

/**
 * @param <Element> uses the passed argument to create a collection element
 * @author Nikita Vasilev
 */
public abstract class Builder<Element> {
    public abstract Element build();
}
