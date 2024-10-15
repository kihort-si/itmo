public class Checker {
    private final Float x;
    private final Float y;
    private final Float r;

    public Checker(Float x, Float y, Float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public boolean check() {
        if (x >= 0 && y >= 0) return (x <= r && y <= r);
        if (x < 0 && y >= 0) return ((x * x + y * y) <= (r * r));
        if (x < 0 && y < 0) return (x <= -r/2 && y <= -r);
        return false;
    }
}
