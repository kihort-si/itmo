import java.util.Arrays;

public class Validation {
    private final Float x;
    private final Float y;
    private final Float r;

    public Validation(Float x, Float y, Float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public boolean valid() {
        return validX() && validY() && validR();
    }

    private boolean validX() {
        Float[] values = {-2f, -1.5f, -1f, -0.5f, 0f, 0.5f, 1f, 1.5f, 2f};
        return Arrays.asList(values).contains(x);
    }

    private boolean validY() {
        return y >= -5 && y <= 5;
    }

    private boolean validR() {
        Float[] values = {1f, 1.5f, 2f, 2.5f, 3f};
        return Arrays.asList(values).contains(r);
    }
}
