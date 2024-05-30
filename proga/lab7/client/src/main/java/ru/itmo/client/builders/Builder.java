package ru.itmo.client.builders;

/**
 * @author Nikita Vasilev
 * @param <Element> uses the passed argument to create a collection element
 */
public abstract class Builder<Element> {
    public abstract Element build();
}
