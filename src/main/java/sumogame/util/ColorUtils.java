package sumogame.util;

import javafx.scene.paint.Color;

public class ColorUtils {

    private ColorUtils() {}

    public static Color darken(Color color, double percent) {
        double factor = 1.0 - (percent / 100.0);
        return Color.color(
                Math.max(0, color.getRed() * factor),
                Math.max(0, color.getGreen() * factor),
                Math.max(0, color.getBlue() * factor),
                color.getOpacity()
        );
    }

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