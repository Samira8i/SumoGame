package sumogame.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import sumogame.model.*;
import sumogame.util.ColorUtils;


public class GameRenderer {
    private Canvas canvas;
    private GraphicsContext gc;

    // Цвета в розовой палитре
    private static final Color BACKGROUND_COLOR = Color.web("#FFF0F5"); // Лавандовый румянец
    private static final Color TEXT_COLOR = Color.web("#8B6969"); // Коричневый
    private static final Color ACCENT_COLOR = Color.web("#FF69B4"); // Ярко-розовый
    private static final Color SHADOW_COLOR = Color.web("#DB7093"); // Темно-розовый
    private static final Color HIGHLIGHT_COLOR = Color.web("#FFC0CB"); // Светло-розовый
    private static final Color PLAYER_PINK = Color.web("#FFB6C1"); // Розовый
    private static final Color PLAYER_GREEN = Color.web("#98FB98"); // Зеленый
    private static final Color PLAYER_BLUE = Color.web("#ADD8E6"); // Голубой

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void render(GameState state, boolean waitingForOpponent) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (waitingForOpponent) {
            // В режиме ожидания рисуем только фон и арену
            drawBackground();
            drawArena(state);

            // Сообщение об ожидании на канвасе
            drawWaitingMessage();
        } else {
            drawGameScreen(state);
        }
    }

    private void drawWaitingMessage() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Полупрозрачный фон
        gc.setFill(Color.rgb(255, 240, 245, 0.8));
        gc.fillRect(0, 0, width, height);

        // Большое сообщение об ожидании
        gc.setFill(ACCENT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⏳ Ожидание противника...", width / 2, height / 2 - 50);

        // Подсказка
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Пожалуйста, подождите подключения второго игрока",
                width / 2, height / 2 + 20);
    }

    private void drawGameScreen(GameState state) {
        drawBackground();
        drawArena(state);

        // Показываем игроков
        if (state.getPlayer1() != null) {
            drawPlayer(state.getPlayer1());
        }
        if (state.getPlayer2() != null) {
            drawPlayer(state.getPlayer2());
        }
    }

    private void drawBackground() {
        // Градиентный фон в розовых тонах
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, BACKGROUND_COLOR),
                new Stop(1, ColorUtils.lighten(BACKGROUND_COLOR, 10))
        );

        gc.setFill(gradient);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Тонкие декоративные линии
        gc.setStroke(HIGHLIGHT_COLOR);
        gc.setLineWidth(1);
        gc.setGlobalAlpha(0.3);
        for (int i = 0; i < 10; i++) {
            double y = canvas.getHeight() * (i + 1) / 11;
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawArena(GameState state) {
        Arena arena = state.getCurrentArena();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

      // использую радиус из объекта Arena, но масштабируем для канваса
        double arenaRadius = arena.getRadius();
        double scale = Math.min(width / arena.getWidth(), height / arena.getHeight());
        double radius = arenaRadius * scale;

        Color arenaColor = Color.web(arena.getType().getColorHex());

        // Фон арены
        RadialGradient arenaGradient = new RadialGradient(
                0, 0, centerX, centerY, radius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, arenaColor.brighter()),
                new Stop(0.7, arenaColor),
                new Stop(1, arenaColor.darker())
        );

        gc.setFill(arenaGradient);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Внешний ободок арены
        gc.setStroke(arenaColor.darker());
        gc.setLineWidth(5);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Внутренний ободок
        gc.setStroke(HIGHLIGHT_COLOR);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius + 3, centerY - radius + 3, radius * 2 - 6, radius * 2 - 6);

        // Центральная линия
        gc.setStroke(ACCENT_COLOR);
        gc.setLineWidth(2);
        gc.setGlobalAlpha(0.5);
        gc.strokeLine(centerX, centerY - radius, centerX, centerY + radius);
        gc.setGlobalAlpha(1.0);

        // Название арены
        String arenaName = arena.getType().getName();
        gc.setFill(ACCENT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("🌸 " + arenaName + " 🌸", centerX, centerY + radius + 35);
    }



    private Color getPlayerColor(Player player) {
        String hex = player.getColorHex();
        switch (hex) {
            case "#FFC0CB": return PLAYER_PINK;
            case "#90EE90": return PLAYER_GREEN;
            case "#ADD8E6": return PLAYER_BLUE;
            default: return PLAYER_PINK;
        }
    }

    private void drawPlayer(Player player) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double arenaWidth = GameConfig.ARENA_WIDTH;
        double arenaHeight = GameConfig.ARENA_HEIGHT;

        // Конвертируем координаты
        double scale = Math.min(width / arenaWidth, height / arenaHeight);
        double x = player.getX() * scale;
        double y = player.getY() * scale;

        // Центрируем арену
        double offsetX = (width - arenaWidth * scale) / 2;
        double offsetY = (height - arenaHeight * scale) / 2;

        x += offsetX;
        y += offsetY;

        // Размер игрока
        double baseSize = 40 * scale;
        double size = baseSize * (player.getCurrentSize() / Player.BASE_SIZE);

        Color playerColor = getPlayerColor(player);
        Color darkColor = ColorUtils.darken(playerColor, 20);

        // Увеличенные пропорции для правдоподобия
        double headSize = size * 0.6;
        double armSize = size * 0.4;
        double legSize = size * 0.4;

        // Тело (центральный круг)
        drawBody(x, y, size, playerColor, darkColor);

        // Пояс (маваси)
        drawMawashi(x, y, size);

        // Руки (шарики по бокам)
        drawArms(x, y, size, armSize, playerColor, darkColor);

        // Ноги (шарики снизу)
        drawLegs(x, y, size, legSize, playerColor, darkColor);

        // Голова
        double headY = y - size * 0.35;
        drawHead(x, headY, headSize, playerColor, darkColor);

        // Лицо
        drawFace(x, headY, headSize, player);

        // Индикатор способности (только если игра началась)
        if (player.isPowerUpActive() || player.isPowerUpAvailable()) {
            drawAbilityIndicator(x, headY - headSize * 0.8, player);
        }

        // Информация об игроке (смещаем ниже, чтобы не перекрывалась)
        drawPlayerInfo(x, y + size * 1.2, player);
    }

    private void drawBody(double x, double y, double size, Color baseColor, Color darkColor) {
        // Градиент для тела
        RadialGradient bodyGradient = new RadialGradient(
                0, 0, x, y, size * 0.8,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, ColorUtils.lighten(baseColor, 20)),
                new Stop(0.7, baseColor),
                new Stop(1, darkColor)
        );

        gc.setFill(bodyGradient);
        gc.fillOval(x - size/2, y - size/2, size, size);

        // Контур тела
        gc.setStroke(darkColor);
        gc.setLineWidth(3);
        gc.strokeOval(x - size/2, y - size/2, size, size);
    }

    private void drawMawashi(double x, double y, double size) {
        // Толстый пояс сумоиста
        gc.setStroke(SHADOW_COLOR);
        gc.setLineWidth(size * 0.08);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Верхняя часть пояса
        gc.strokeLine(x - size * 0.4, y - size * 0.1, x + size * 0.4, y - size * 0.1);
        // Нижняя часть пояса
        gc.strokeLine(x - size * 0.4, y + size * 0.1, x + size * 0.4, y + size * 0.1);
    }

    private void drawArms(double x, double y, double bodySize, double armSize, Color color, Color darkColor) {
        // Левая рука (шарик слева)
        double leftX = x - bodySize * 0.45;
        drawArmBall(leftX, y, armSize, color, darkColor);

        // Правая рука (шарик справа)
        double rightX = x + bodySize * 0.45;
        drawArmBall(rightX, y, armSize, color, darkColor);
    }

    private void drawArmBall(double x, double y, double size, Color color, Color darkColor) {
        RadialGradient armGradient = new RadialGradient(
                0, 0, x, y, size * 0.8,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, ColorUtils.lighten(color, 30)),
                new Stop(0.7, color),
                new Stop(1, darkColor)
        );

        gc.setFill(armGradient);
        gc.fillOval(x - size/2, y - size/2, size, size);

        gc.setStroke(darkColor);
        gc.setLineWidth(2);
        gc.strokeOval(x - size/2, y - size/2, size, size);
    }

    private void drawLegs(double x, double y, double bodySize, double legSize, Color color, Color darkColor) {
        // Левая нога (шарик слева снизу)
        double leftX = x - bodySize * 0.35;
        double leftY = y + bodySize * 0.45;
        drawLegBall(leftX, leftY, legSize, color, darkColor);

        // Правая нога (шарик справа снизу)
        double rightX = x + bodySize * 0.35;
        double rightY = y + bodySize * 0.45;
        drawLegBall(rightX, rightY, legSize, color, darkColor);
    }

    private void drawLegBall(double x, double y, double size, Color color, Color darkColor) {
        RadialGradient legGradient = new RadialGradient(
                0, 0, x, y, size * 0.8,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, ColorUtils.lighten(color, 30)),
                new Stop(0.7, color),
                new Stop(1, darkColor)
        );

        gc.setFill(legGradient);
        gc.fillOval(x - size/2, y - size/2, size, size);

        gc.setStroke(darkColor);
        gc.setLineWidth(2);
        gc.strokeOval(x - size/2, y - size/2, size, size);
    }

    private void drawHead(double x, double y, double size, Color baseColor, Color darkColor) {
        // Градиент для головы
        RadialGradient headGradient = new RadialGradient(
                0, 0, x, y, size * 0.8,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, ColorUtils.lighten(baseColor, 30)),
                new Stop(0.7, baseColor),
                new Stop(1, darkColor)
        );

        gc.setFill(headGradient);
        gc.fillOval(x - size/2, y - size/2, size, size);

        // Контур головы
        gc.setStroke(darkColor);
        gc.setLineWidth(3);
        gc.strokeOval(x - size/2, y - size/2, size, size);
    }

    private void drawFace(double x, double y, double size, Player player) {
        // Глаза
        double eyeY = y + size * 0.1;
        double eyeSize = size * 0.12;

        gc.setFill(Color.BLACK);
        gc.fillOval(x - size * 0.25 - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
        gc.fillOval(x + size * 0.25 - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);

        // Рот
        double mouthWidth = size * 0.4;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(size * 0.02);
        gc.strokeLine(x - mouthWidth/2, y + size * 0.2, x + mouthWidth/2, y + size * 0.2);
    }

    private void drawAbilityIndicator(double x, double y, Player player) {
        double size = 20;

        if (player.isPowerUpActive()) {
            // Активная способность
            gc.setFill(ACCENT_COLOR);
            gc.setGlobalAlpha(0.6);
            gc.fillOval(x - size, y - size, size * 2, size * 2);
            gc.setGlobalAlpha(1.0);

            gc.setFill(Color.WHITE);
            gc.fillOval(x - size/2, y - size/2, size, size);

            gc.setFill(ACCENT_COLOR);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.7));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("⚡", x, y + size * 0.3);
        } else if (player.isPowerUpAvailable()) {
            // Доступная способность
            gc.setFill(HIGHLIGHT_COLOR);
            gc.fillOval(x - size/2, y - size/2, size, size);

            gc.setFill(ACCENT_COLOR);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.8));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("✓", x, y + size * 0.3);
        }
    }

    private void drawPlayerInfo(double x, double y, Player player) {
        // Фон для информации
        gc.setFill(Color.rgb(255, 255, 255, 0.8));
        gc.fillRoundRect(x - 50, y - 15, 100, 30, 10, 10);

        // Рамка
        gc.setStroke(ACCENT_COLOR);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x - 50, y - 15, 100, 30, 10, 10);

        // Имя и номер
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Игрок " + player.getPlayerId(), x, y + 5);
    }
}