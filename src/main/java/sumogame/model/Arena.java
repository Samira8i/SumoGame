package sumogame.model;

public class Arena {
    private final ArenaType type;
    private final double width;
    private final double height;
    private final double centerX;
    private final double centerY;
    private final double radius;

    public Arena(ArenaType type) {
        this.type = type;
        this.width = GameConfig.ARENA_WIDTH;
        this.height = GameConfig.ARENA_HEIGHT;
        this.centerX = width / 2;
        this.centerY = height / 2;
        this.radius = Math.min(width, height) * 0.35;
    }

    public boolean isPointInside(double x, double y) {
        double dx = x - centerX;
        double dy = y - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    public boolean isPlayerOut(Player player) {
        return !isPointInside(player.getX(), player.getY());
    }

    public double getPlayer1StartX() {
        return centerX - radius * 0.7;
    }

    public double getPlayer1StartY() {
        return centerY;
    }

    public double getPlayer2StartX() {
        return centerX + radius * 0.7;
    }

    public double getPlayer2StartY() {
        return centerY;
    }

    public ArenaType getType() { return type; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getRadius() { return radius; }
}