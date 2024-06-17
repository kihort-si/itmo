package ru.itmo.common.network.response;

import java.io.Serializable;
import java.util.Objects;

/**
 * The response that is sent to the client after the corresponding commands are executed.
 *
 * @author Nikita Vasilev
 */
public abstract class Response implements Serializable {
    public final String name;
    private final String error;
    private final int status;

    public Response(String name, String error, int status) {
        this.name = name;
        this.error = error;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
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
