package sumogame.view;

import javafx.scene.paint.Color;

/**
 * Палитра цветов игры в пастельных тонах
 */
public class GameColors {
    // Основные цвета
    public static final Color BACKGROUND = Color.web("#2D3047"); // Темно-синий
    public static final Color ARENA_BASE = Color.web("#F8F9FA"); // Белый снег
    public static final Color TEXT_PRIMARY = Color.web("#E9ECEF"); // Светлый текст
    public static final Color TEXT_SECONDARY = Color.web("#ADB5BD"); // Серый текст

    // Пастельные цвета игроков
    public static final Color PINK_PLAYER = Color.web("#FFB6C1"); // Пастельный розовый
    public static final Color GREEN_PLAYER = Color.web("#98FB98"); // Пастельный зеленый
    public static final Color BLUE_PLAYER = Color.web("#ADD8E6"); // Пастельный голубой

    // Пастельные цвета арен
    public static final Color PINK_ARENA = Color.web("#FFD1DC"); // Светло-розовый
    public static final Color PEACH_ARENA = Color.web("#FFDAB9"); // Персиковый
    public static final Color LAVENDER_ARENA = Color.web("#E6E6FA"); // Лавандовый

    // Цвета способностей
    public static final Color ABILITY_ACTIVE = Color.web("#FFD700"); // Золотой
    public static final Color ABILITY_READY = Color.web("#90EE90"); // Светло-зеленый
    public static final Color ABILITY_USED = Color.web("#A9A9A9"); // Серый

    // Дополнительные цвета
    public static final Color OUTLINE = Color.web("#495057"); // Темно-серый для контуров
    public static final Color HIGHLIGHT = Color.web("#FFE5B4"); // Светло-оранжевый для выделения
    public static final Color SHADOW = Color.web("#212529"); // Черный для теней

    // Градиенты для эффектов
    public static Color[] getPlayerGradient(Color baseColor) {
        return new Color[] {
                baseColor,
                baseColor.brighter(),
                baseColor.darker()
        };
    }
}