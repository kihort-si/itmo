package ru.itmo.programming.common.network.response;

import java.io.Serializable;
import java.util.Objects;

/**
 * The response that is sent to the client after the corresponding commands are executed.
 * @author Nikita Vasilev
 */
public class Response implements Serializable {
    public final String name;
    private final String error;
    public Response (String name, String error) {
        this.name = name;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public String getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Objects.equals(name, response.name) && Objects.equals(error, response.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, error);
    }

    @Override
    public String toString() {
        return "Response{" +
                "name='" + name + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
