package sumogame.model;

import sumogame.util.DebugLogger;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    public static Direction fromString(String direction) {
        try {
            return Direction.valueOf(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            DebugLogger.error("Неизвестное направление: " + direction);
            return null;
        }
    }
}