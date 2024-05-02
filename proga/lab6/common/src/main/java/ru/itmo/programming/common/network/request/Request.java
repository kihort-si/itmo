package ru.itmo.programming.common.network.request;

import java.io.Serializable;
import java.util.Objects;

/**
 * A request that is passed to the server to execute the appropriate commands.
 * @author Nikita Vasilev
 */
public class Request implements Serializable {
    private final String name;

    public Request(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(name, request.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Request{" +
                "name='" + name + '\'' +
                '}';
    }
}
