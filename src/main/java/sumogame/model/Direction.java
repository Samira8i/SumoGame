package sumogame.model;

public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public double[] calculateNewPosition(double currentX, double currentY, double speed) {
        return new double[]{
                currentX + (dx * speed),
                currentY + (dy * speed)
        };
    }
}