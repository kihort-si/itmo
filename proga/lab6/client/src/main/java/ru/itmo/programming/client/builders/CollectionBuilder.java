package ru.itmo.programming.client.builders;

/**
 * @author Nikita Vasilev
 * @param <Element> uses the passed argument to create a collection element
 */
public abstract class CollectionBuilder<Element> {
    public abstract Element build();
}
