package sumogame.util;

import javafx.scene.paint.Color;

/**
 * Утилита для работы с цветами (преобразование между моделью и представлением)
 */
public class ColorUtils {

    private ColorUtils() {}

    /**
     * Конвертирует HEX строку в JavaFX Color
     */
    public static Color hexToColor(String hex) {
        try {
            return Color.web(hex);
        } catch (IllegalArgumentException e) {
            DebugLogger.error("Некорректный HEX цвет: " + hex);
            return Color.GRAY;
        }
    }

    /**
     * Конвертирует JavaFX Color в HEX строку
     */
    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Затемняет цвет на указанный процент
     */
    public static Color darken(Color color, double percent) {
        double factor = 1.0 - (percent / 100.0);
        return Color.color(
                Math.max(0, color.getRed() * factor),
                Math.max(0, color.getGreen() * factor),
                Math.max(0, color.getBlue() * factor),
                color.getOpacity()
        );
    }

    /**
     * Осветляет цвет на указанный процент
     */
    public static Color lighten(Color color, double percent) {
        double factor = 1.0 + (percent / 100.0);
        return Color.color(
                Math.min(1, color.getRed() * factor),
                Math.min(1, color.getGreen() * factor),
                Math.min(1, color.getBlue() * factor),
                color.getOpacity()
        );
    }
}