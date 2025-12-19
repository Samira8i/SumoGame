package sumogame.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.*;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import sumogame.model.*;
import sumogame.util.ColorUtils;

public class GameRenderer {
    private Canvas canvas;
    private GraphicsContext gc;
    private long lastTime = System.currentTimeMillis();
    private double pulsePhase = 0;

    // Эффекты для переиспользования
    private DropShadow playerShadow;
    private DropShadow arenaGlow;
    private DropShadow textShadow;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        createEffects();
    }

    private void createEffects() {
        // Тень для игроков
        playerShadow = new DropShadow();
        playerShadow.setColor(GameColors.SHADOW);
        playerShadow.setRadius(10);
        playerShadow.setOffsetX(3);
        playerShadow.setOffsetY(3);
        playerShadow.setBlurType(BlurType.GAUSSIAN);

        // Свечение для арены
        arenaGlow = new DropShadow();
        arenaGlow.setColor(Color.rgb(255, 255, 255, 0.3));
        arenaGlow.setRadius(20);
        arenaGlow.setOffsetX(0);
        arenaGlow.setOffsetY(0);

        // Тень для текста
        textShadow = new DropShadow();
        textShadow.setColor(GameColors.SHADOW);
        textShadow.setRadius(3);
        textShadow.setOffsetX(1);
        textShadow.setOffsetY(1);
    }

    public void render(GameState state) {
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastTime) / 1000.0;
        lastTime = currentTime;

        pulsePhase = (pulsePhase + deltaTime * 2) % (Math.PI * 2);

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawBackground();
        drawArena(state);
        drawPlayerWithDetails(state.getPlayer1());
        drawPlayerWithDetails(state.getPlayer2());
        drawUI(state);
    }

    private void drawBackground() {
        // Градиентный фон
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, GameColors.BACKGROUND.brighter()),
                new Stop(1, GameColors.BACKGROUND.darker())
        );

        gc.setFill(gradient);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Звезды на фоне
        gc.setFill(GameColors.TEXT_PRIMARY);
        gc.setGlobalAlpha(0.3);
        for (int i = 0; i < 50; i++) {
            double x = (i * 37) % canvas.getWidth();
            double y = (i * 23) % canvas.getHeight();
            double size = 1 + Math.sin(i + pulsePhase) * 0.5;
            gc.fillOval(x, y, size, size);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawArena(GameState state) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        ArenaType arena = state.getCurrentArena();
        if (arena == null) {
            arena = ArenaType.PINK_CIRCLE;
        }

        // Получаем цвет арены из палитры
        Color arenaColor = getArenaColor(arena);
        double radius = Math.min(width, height) * 0.42;
        double pulseRadius = radius * (1 + Math.sin(pulsePhase) * 0.02);

        // Внешнее свечение
        gc.setEffect(arenaGlow);
        gc.setFill(arenaColor.deriveColor(0, 1, 1, 0.2));
        gc.fillOval(centerX - pulseRadius * 1.15, centerY - pulseRadius * 1.15,
                pulseRadius * 2.3, pulseRadius * 2.3);
        gc.setEffect(null);

        // Основной круг арены (с градиентом)
        RadialGradient arenaGradient = new RadialGradient(
                0, 0, centerX, centerY, pulseRadius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, arenaColor.brighter()),
                new Stop(0.7, arenaColor),
                new Stop(1, arenaColor.darker())
        );

        gc.setFill(arenaGradient);
        gc.fillOval(centerX - pulseRadius, centerY - pulseRadius,
                pulseRadius * 2, pulseRadius * 2);

        // Внутренний ободок
        gc.setStroke(GameColors.TEXT_PRIMARY);
        gc.setLineWidth(3);
        gc.setLineDashOffset(pulsePhase * 10);
        gc.strokeOval(centerX - pulseRadius, centerY - pulseRadius,
                pulseRadius * 2, pulseRadius * 2);

        // Центральная линия
        gc.setStroke(GameColors.TEXT_PRIMARY);
        gc.setLineWidth(2);
        gc.setGlobalAlpha(0.5);
        gc.strokeLine(centerX, centerY - pulseRadius, centerX, centerY + pulseRadius);
        gc.setGlobalAlpha(1.0);

        // Текстура арены (светлые точки)
        gc.setFill(GameColors.TEXT_PRIMARY);
        gc.setGlobalAlpha(0.1);
        for (int i = 0; i < 30; i++) {
            double angle = i * Math.PI * 2 / 30;
            double x = centerX + Math.cos(angle) * pulseRadius * 0.8;
            double y = centerY + Math.sin(angle) * pulseRadius * 0.8;
            gc.fillOval(x - 3, y - 3, 6, 6);
        }
        gc.setGlobalAlpha(1.0);

        // Название арены
        gc.setEffect(textShadow);
        gc.setFill(GameColors.TEXT_PRIMARY);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(arena.getName(), centerX, 50);
        gc.setEffect(null);
    }

    private Color getArenaColor(ArenaType arena) {
        switch (arena.getName()) {
            case "Розовая арена": return GameColors.PINK_ARENA;
            case "Персиковая арена": return GameColors.PEACH_ARENA;
            case "Красная арена": return GameColors.LAVENDER_ARENA;
            default: return GameColors.PINK_ARENA;
        }
    }

    private Color getPlayerColor(Player player) {
        String hex = player.getColorHex();
        switch (hex) {
            case "#FFC0CB": return GameColors.PINK_PLAYER;
            case "#90EE90": return GameColors.GREEN_PLAYER;
            case "#ADD8E6": return GameColors.BLUE_PLAYER;
            default: return GameColors.PINK_PLAYER;
        }
    }

    private void drawPlayerWithDetails(Player player) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double arenaWidth = GameConfig.ARENA_WIDTH;
        double arenaHeight = GameConfig.ARENA_HEIGHT;

        // Конвертируем координаты с учетом масштаба
        double scale = Math.min(width / arenaWidth, height / arenaHeight);
        double x = player.getX() * scale;
        double y = player.getY() * scale;

        // Размер игрока (увеличиваем на 20%)
        double baseSize = 30 * scale * 1.2;
        double size = baseSize * (player.getCurrentSize() / Player.BASE_SIZE);

        // Получаем цвета
        Color playerColor = getPlayerColor(player);
        Color darkColor = ColorUtils.darken(playerColor, 25);
        Color lightColor = ColorUtils.lighten(playerColor, 25);

        // Позиции частей тела
        double bodyX = x;
        double bodyY = y;
        double bodySize = size;

        // Голова (25% от размера тела)
        double headSize = bodySize * 0.25;
        double headY = bodyY - bodySize * 0.5 - headSize * 0.3;

        // Ноги
        double legWidth = bodySize * 0.15;
        double legHeight = bodySize * 0.25;
        double legY = bodyY + bodySize * 0.5 - legHeight * 0.3;

        // Руки
        double armLength = bodySize * 0.35;
        double armWidth = bodySize * 0.12;

        // Эффект свечения для активной способности
        if (player.isPowerUpActive()) {
            drawAbilityEffect(bodyX, bodyY, bodySize, playerColor);
        }

        // Тело (основной овал)
        drawBody(bodyX, bodyY, bodySize, playerColor, darkColor);

        // Пояс (маваси)
        drawMawashi(bodyX, bodyY, bodySize);

        // Ноги
        drawLegs(bodyX, legY, legWidth, legHeight, darkColor);

        // Руки
        drawArms(bodyX, bodyY, armLength, armWidth, darkColor, player.getPlayerId());

        // Голова
        drawHead(bodyX, headY, headSize, playerColor, darkColor, player.getPlayerId());

        // Выражение лица (в зависимости от состояния)
        drawFace(bodyX, headY, headSize, player);

        // Индикатор способности над головой
        drawAbilityIndicator(bodyX, headY - headSize, player);

        // Имя и номер игрока
        drawPlayerInfo(bodyX, bodyY + bodySize * 0.8, player);
    }

    private void drawBody(double x, double y, double size, Color baseColor, Color darkColor) {
        // Тень тела
        gc.setEffect(playerShadow);
        gc.setFill(darkColor);
        gc.fillOval(x - size * 0.55, y - size * 0.55, size * 1.1, size * 1.1);
        gc.setEffect(null);

        // Градиент для тела
        RadialGradient bodyGradient = new RadialGradient(
                0, 0, x - size * 0.2, y - size * 0.2, size * 0.8,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, baseColor.brighter()),
                new Stop(0.6, baseColor),
                new Stop(1, darkColor)
        );

        gc.setFill(bodyGradient);
        gc.fillOval(x - size * 0.5, y - size * 0.5, size, size);

        // Контур тела
        gc.setStroke(darkColor);
        gc.setLineWidth(2);
        gc.strokeOval(x - size * 0.5, y - size * 0.5, size, size);

        // Детали живота (светлые блики)
        gc.setFill(baseColor.brighter());
        gc.setGlobalAlpha(0.3);
        gc.fillOval(x - size * 0.3, y - size * 0.2, size * 0.6, size * 0.4);
        gc.setGlobalAlpha(1.0);
    }

    private void drawMawashi(double x, double y, double size) {
        // Основной пояс
        gc.setStroke(GameColors.OUTLINE);
        gc.setLineWidth(size * 0.05);

        // Верхняя часть пояса
        gc.strokeLine(x - size * 0.4, y - size * 0.1, x + size * 0.4, y - size * 0.1);
        // Нижняя часть пояса
        gc.strokeLine(x - size * 0.4, y + size * 0.1, x + size * 0.4, y + size * 0.1);
        // Боковые части
        gc.strokeLine(x - size * 0.4, y - size * 0.1, x - size * 0.4, y + size * 0.1);
        gc.strokeLine(x + size * 0.4, y - size * 0.1, x + size * 0.4, y + size * 0.1);

        // Узоры на поясе
        gc.setFill(GameColors.HIGHLIGHT);
        for (int i = -2; i <= 2; i++) {
            double patternX = x + i * size * 0.15;
            if (i == 0) continue; // Пропускаем центр

            // Ромбики на поясе
            gc.fillRect(patternX - size * 0.03, y - size * 0.08, size * 0.06, size * 0.16);
        }
    }

    private void drawLegs(double x, double y, double width, double height, Color color) {
        // Левая нога
        gc.setFill(color);
        double leftX = x - width * 0.7;
        gc.fillRoundRect(leftX - width/2, y, width, height, 15, 15);

        // Правая нога
        double rightX = x + width * 0.7;
        gc.fillRoundRect(rightX - width/2, y, width, height, 15, 15);

        // Контур ног
        gc.setStroke(ColorUtils.darken(color, 40));
        gc.setLineWidth(1);
        gc.strokeRoundRect(leftX - width/2, y, width, height, 15, 15);
        gc.strokeRoundRect(rightX - width/2, y, width, height, 15, 15);

        // Складки на ногах
        gc.setStroke(GameColors.OUTLINE);
        gc.setLineWidth(0.5);
        gc.strokeLine(leftX - width * 0.2, y + height * 0.3, leftX + width * 0.2, y + height * 0.3);
        gc.strokeLine(rightX - width * 0.2, y + height * 0.3, rightX + width * 0.2, y + height * 0.3);
    }

    private void drawArms(double x, double y, double length, double width, Color color, int playerId) {
        // Направление рук зависит от игрока
        double direction = playerId == 1 ? 1 : -1;
        double angle = direction * 0.3; // Угол разведения рук

        // Левая рука
        double leftX = x - Math.cos(angle) * length * 0.7;
        double leftY = y + Math.sin(angle) * length * 0.7;
        drawArm(leftX, leftY, width, color, -direction);

        // Правая рука
        double rightX = x + Math.cos(angle) * length * 0.7;
        double rightY = y + Math.sin(angle) * length * 0.7;
        drawArm(rightX, rightY, width, color, direction);
    }

    private void drawArm(double x, double y, double width, Color color, double direction) {
        // Плечо
        gc.setFill(color);
        gc.fillOval(x - width/2, y - width/2, width, width);

        // Предплечье
        double forearmX = x + direction * width * 2;
        double forearmY = y;

        // Градиент для руки
        LinearGradient armGradient = new LinearGradient(
                x, y, forearmX, forearmY, false, CycleMethod.NO_CYCLE,
                new Stop(0, color),
                new Stop(1, ColorUtils.darken(color, 20))
        );

        gc.setFill(armGradient);
        gc.fillRoundRect(
                Math.min(x, forearmX) - width/2,
                y - width/2,
                Math.abs(forearmX - x) + width,
                width,
                width/2,
                width/2
        );

        // Кисть
        double handX = forearmX + direction * width * 1.5;
        gc.fillOval(handX - width * 0.6, y - width * 0.6, width * 1.2, width * 1.2);

        // Пальцы
        gc.setFill(ColorUtils.darken(color, 30));
        for (int i = 0; i < 3; i++) {
            double fingerX = handX + direction * width * 0.8;
            double fingerY = y - width * 0.3 + i * width * 0.3;
            gc.fillOval(fingerX - width * 0.15, fingerY - width * 0.15, width * 0.3, width * 0.3);
        }

        // Контур руки
        gc.setStroke(ColorUtils.darken(color, 40));
        gc.setLineWidth(1);
        gc.strokeOval(x - width/2, y - width/2, width, width);
        gc.strokeOval(handX - width * 0.6, y - width * 0.6, width * 1.2, width * 1.2);
    }

    private void drawHead(double x, double y, double size, Color baseColor, Color darkColor, int playerId) {
        // Градиент для головы
        RadialGradient headGradient = new RadialGradient(
                0, 0, x, y - size * 0.2, size * 0.8,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, baseColor.brighter()),
                new Stop(0.7, baseColor),
                new Stop(1, darkColor)
        );

        // Голова
        gc.setFill(headGradient);
        gc.fillOval(x - size/2, y - size/2, size, size);

        // Контур головы
        gc.setStroke(darkColor);
        gc.setLineWidth(2);
        gc.strokeOval(x - size/2, y - size/2, size, size);

        // Волосы (чуб)
        gc.setFill(darkColor);
        for (int i = 0; i < 5; i++) {
            double hairX = x - size * 0.3 + i * size * 0.15;
            double hairY = y - size * 0.4;
            gc.fillOval(hairX - size * 0.05, hairY - size * 0.05, size * 0.1, size * 0.15);
        }

        // Уши
        drawEar(x - size * 0.45, y, size * 0.15, darkColor);
        drawEar(x + size * 0.45, y, size * 0.15, darkColor);
    }

    private void drawEar(double x, double y, double size, Color color) {
        gc.setFill(color);
        gc.fillOval(x - size/2, y - size/2, size, size);

        // Внутренняя часть уха
        gc.setFill(ColorUtils.lighten(color, 20));
        gc.fillOval(x - size * 0.3, y - size * 0.3, size * 0.6, size * 0.6);
    }

    private void drawFace(double x, double y, double size, Player player) {
        // Глаза
        double eyeSize = size * 0.1;
        double eyeY = y - size * 0.05;

        // Левый глаз
        double leftEyeX = x - size * 0.2;
        drawEye(leftEyeX, eyeY, eyeSize, player);

        // Правый глаз
        double rightEyeX = x + size * 0.2;
        drawEye(rightEyeX, eyeY, eyeSize, player);

        // Рот (выражение зависит от состояния)
        drawMouth(x, y + size * 0.15, size * 0.2, player);

        // Щеки (легкий румянец)
        gc.setFill(Color.rgb(255, 192, 203, 0.3));
        gc.fillOval(x - size * 0.35, y + size * 0.05, size * 0.2, size * 0.1);
        gc.fillOval(x + size * 0.15, y + size * 0.05, size * 0.2, size * 0.1);
    }

    private void drawEye(double x, double y, double size, Player player) {
        // Белок глаза
        gc.setFill(Color.WHITE);
        gc.fillOval(x - size, y - size, size * 2, size * 2);

        // Радужка (цвет зависит от игрока)
        Color irisColor = getPlayerColor(player).darker();
        gc.setFill(irisColor);
        double irisSize = size * 1.2;
        gc.fillOval(x - irisSize/2, y - irisSize/2, irisSize, irisSize);

        // Зрачок
        gc.setFill(Color.BLACK);
        double pupilSize = size * 0.6;
        gc.fillOval(x - pupilSize/2, y - pupilSize/2, pupilSize, pupilSize);

        // Блик в глазу
        gc.setFill(Color.WHITE);
        double highlightSize = size * 0.3;
        gc.fillOval(x - size * 0.3, y - size * 0.4, highlightSize, highlightSize);

        // Контур глаза
        gc.setStroke(GameColors.OUTLINE);
        gc.setLineWidth(1);
        gc.strokeOval(x - size, y - size, size * 2, size * 2);

        // Ресницы (если злой)
        if (player.isPowerUpActive()) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            // Верхние ресницы
            for (int i = -1; i <= 1; i++) {
                double angle = i * 0.2;
                double endX = x + Math.cos(angle) * size * 1.5;
                double endY = y - size + Math.sin(angle) * size * 0.5;
                gc.strokeLine(x + i * size * 0.2, y - size, endX, endY);
            }
        }
    }

    private void drawMouth(double x, double y, double size, Player player) {
        if (player.isPowerUpActive()) {
            // Злой рот (прямая линия)
            gc.setStroke(Color.rgb(200, 0, 0));
            gc.setLineWidth(2);
            gc.strokeLine(x - size * 0.8, y, x + size * 0.8, y);
            // Уголки рта вниз
            gc.strokeLine(x - size * 0.8, y, x - size * 0.6, y - size * 0.2);
            gc.strokeLine(x + size * 0.8, y, x + size * 0.6, y - size * 0.2);
        } else {
            // Улыбка
            gc.setStroke(Color.rgb(150, 0, 0));
            gc.setLineWidth(2);
            // Основная дуга улыбки
            gc.strokeArc(x - size * 0.6, y - size * 0.2, size * 1.2, size * 0.8, 0, -180, ArcType.OPEN);
            // Уголки улыбки
            gc.strokeLine(x - size * 0.6, y, x - size * 0.5, y - size * 0.1);
            gc.strokeLine(x + size * 0.6, y, x + size * 0.5, y - size * 0.1);
        }
    }

    private void drawAbilityEffect(double x, double y, double size, Color color) {
        double pulse = Math.sin(pulsePhase * 3) * 0.2 + 0.8;

        // Внешнее свечение
        gc.setEffect(new DropShadow(BlurType.GAUSSIAN, color.deriveColor(0, 1, 1, 0.5),
                20 * pulse, 0.8, 0, 0));
        gc.setFill(color.deriveColor(0, 1, 1, 0.2));
        gc.fillOval(x - size * 0.7, y - size * 0.7, size * 1.4, size * 1.4);
        gc.setEffect(null);

        // Внутренние круги энергии
        gc.setStroke(color.deriveColor(0, 1, 1, 0.6));
        gc.setLineWidth(2);
        for (int i = 0; i < 3; i++) {
            double offset = (pulsePhase + i * Math.PI * 2 / 3) % (Math.PI * 2);
            double ringSize = size * (0.6 + Math.sin(offset) * 0.2);
            gc.setLineDashOffset(pulsePhase * 20);
            gc.strokeOval(x - ringSize / 2, y - ringSize / 2, ringSize, ringSize);
        }
    }

    private void drawAbilityIndicator(double x, double y, Player player) {
        double size = 15;
        double pulse = Math.sin(pulsePhase * 4) * 0.3 + 0.7;

        if (player.isPowerUpActive()) {
            // Активная способность - вращающаяся звезда
            gc.save();
            gc.translate(x, y);
            gc.rotate(pulsePhase * 50);

            // Звезда
            gc.setFill(GameColors.ABILITY_ACTIVE);
            drawStar(0, 0, size * pulse, size * pulse * 0.5, 5);

            gc.restore();

            // Таймер
            double remainingTime = player.getPowerUpRemainingTime();
            gc.setFill(GameColors.TEXT_PRIMARY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.format("%.1f", remainingTime), x, y + size * 1.5);

        } else if (player.isPowerUpAvailable()) {
            // Доступная способность - мерцающий круг
            gc.setFill(GameColors.ABILITY_READY.deriveColor(0, 1, 1, pulse));
            gc.fillOval(x - size/2, y - size/2, size, size);

            // Значок готовности
            gc.setFill(GameColors.TEXT_PRIMARY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("✓", x, y + size * 0.3);
        }
    }

    private void drawStar(double x, double y, double outerRadius, double innerRadius, int points) {
        double[] xPoints = new double[points * 2];
        double[] yPoints = new double[points * 2];

        for (int i = 0; i < points * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = Math.PI * i / points;
            xPoints[i] = x + radius * Math.cos(angle);
            yPoints[i] = y + radius * Math.sin(angle);
        }

        gc.fillPolygon(xPoints, yPoints, points * 2);
    }

    private void drawPlayerInfo(double x, double y, Player player) {
        // Фон для информации
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(x - 40, y - 10, 80, 25, 10, 10);

        // Имя игрока
        gc.setFill(GameColors.TEXT_PRIMARY);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Игрок " + player.getPlayerId(), x, y + 5);

        // Способность под именем
        gc.setFill(GameColors.TEXT_SECONDARY);
        gc.setFont(Font.font("Arial", 9));
        gc.fillText(player.getType().getAbilityName(), x, y + 15);
    }

    private void drawUI(GameState state) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Панель счета
        drawScorePanel(width, height, state);

        // Панель времени и раунда
        drawTimePanel(width, height, state);

        // Прогресс раундов
        drawRoundProgress(width, height, state);
    }

    private void drawScorePanel(double width, double height, GameState state) {
        double panelY = 80;
        double player1X = width * 0.25;
        double player2X = width * 0.75;

        // Фон панелей
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRoundRect(player1X - 60, panelY - 25, 120, 50, 15, 15);
        gc.fillRoundRect(player2X - 60, panelY - 25, 120, 50, 15, 15);

        // Имя и счет игрока 1
        Color player1Color = getPlayerColor(state.getPlayer1());
        gc.setFill(player1Color);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Игрок 1", player1X, panelY - 5);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText(String.valueOf(state.getPlayer1Score()), player1X, panelY + 15);

        // Имя и счет игрока 2
        Color player2Color = getPlayerColor(state.getPlayer2());
        gc.setFill(player2Color);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("Игрок 2", player2X, panelY - 5);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText(String.valueOf(state.getPlayer2Score()), player2X, panelY + 15);

        // Разделитель
        gc.setFill(GameColors.TEXT_PRIMARY);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText(":", width / 2, panelY + 5);
    }

    private void drawTimePanel(double width, double height, GameState state) {
        double panelY = 20;

        // Фон
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillRoundRect(width/2 - 120, panelY - 20, 240, 40, 20, 20);

        // Таймер
        int timeLeft = (int) Math.ceil(state.getRoundTime());
        Color timeColor = timeLeft <= 10 ? Color.RED : GameColors.TEXT_PRIMARY;

        gc.setFill(timeColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Время: " + timeLeft + "с", width / 2, panelY + 5);

        // Номер раунда
        gc.setFill(GameColors.TEXT_SECONDARY);
        gc.setFont(Font.font("Arial", 14));
        String roundInfo = "Раунд " + state.getRoundNumber() + " из 3";
        if (state.getCurrentArena() != null) {
            roundInfo += " - " + state.getCurrentArena().getName();
        }
        gc.fillText(roundInfo, width / 2, panelY + 25);
    }

    private void drawRoundProgress(double width, double height, GameState state) {
        double startX = width / 2 - 100;
        double y = height - 40;
        double circleRadius = 12;
        double spacing = 50;

        int[] winners = state.getRoundWinners();

        for (int i = 0; i < 3; i++) {
            double x = startX + i * spacing;

            // Цвет круга в зависимости от результата
            Color circleColor;
            if (i < state.getRoundNumber() - 1) {
                switch (winners[i]) {
                    case 1: circleColor = getPlayerColor(state.getPlayer1()); break;
                    case 2: circleColor = getPlayerColor(state.getPlayer2()); break;
                    case 0: circleColor = GameColors.TEXT_SECONDARY; break;
                    default: circleColor = GameColors.ABILITY_USED;
                }
            } else if (i == state.getRoundNumber() - 1) {
                // Текущий раунд пульсирует
                double pulse = Math.sin(pulsePhase * 3) * 0.2 + 0.8;
                circleColor = GameColors.ABILITY_ACTIVE.deriveColor(0, 1, 1, pulse);
            } else {
                circleColor = GameColors.ABILITY_USED;
            }

            // Рисуем круг
            gc.setFill(circleColor);
            gc.fillOval(x - circleRadius, y - circleRadius, circleRadius * 2, circleRadius * 2);

            // Контур
            gc.setStroke(GameColors.OUTLINE);
            gc.setLineWidth(2);
            gc.strokeOval(x - circleRadius, y - circleRadius, circleRadius * 2, circleRadius * 2);

            // Номер раунда
            gc.setFill(GameColors.TEXT_PRIMARY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(i + 1), x, y + 3);
        }
    }

    public void resize(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
    }
}