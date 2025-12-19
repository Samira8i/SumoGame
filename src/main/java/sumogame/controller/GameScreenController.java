package sumogame.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import sumogame.view.GameRenderer;

public class GameScreenController {
    @FXML private Canvas gameCanvas;
    @FXML private StackPane gameContainer;
    @FXML private Label player1ScoreLabel;
    @FXML private Label player2ScoreLabel;
    @FXML private Label roundTimeLabel;
    @FXML private Label roundNumberLabel;
    @FXML private ProgressBar player1PowerUpBar;
    @FXML private ProgressBar player2PowerUpBar;
    @FXML private Label powerUpStatusLabel;
    @FXML private Label powerUpTimerLabel;

    private GameRenderer gameRenderer;
    private GameController gameController;
    private boolean isLocalPlayer1;

    @FXML
    public void initialize() {
        // Создаем рендерер
        gameRenderer = new GameRenderer(gameCanvas);

        // Настраиваем размеры канваса
        gameCanvas.widthProperty().bind(gameContainer.widthProperty());
        gameCanvas.heightProperty().bind(gameContainer.heightProperty());

        // Обработка изменения размеров
        gameCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            gameRenderer.resize(newVal.doubleValue(), gameCanvas.getHeight());
        });

        gameCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            gameRenderer.resize(gameCanvas.getWidth(), newVal.doubleValue());
        });

        // Инициализируем индикаторы способностей
        updatePowerUpUI();
    }

    @FXML
    public void handleKeyPressed(KeyEvent event) {
        if (gameController == null) return;

        String direction = null;

        switch (event.getCode()) {
            case W:
            case UP:
                direction = "UP";
                break;
            case S:
            case DOWN:
                direction = "DOWN";
                break;
            case A:
            case LEFT:
                direction = "LEFT";
                break;
            case D:
            case RIGHT:
                direction = "RIGHT";
                break;
            case SPACE:
                System.out.println("Нажата клавиша ПРОБЕЛ для активации способности");
                gameController.activatePowerUp();
                updatePowerUpUI();
                break;
        }

        if (direction != null) {
            gameController.handlePlayerInput(direction);
        }
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
        this.isLocalPlayer1 = controller != null; // Логика определения кто локальный игрок

        // Настраиваем обновление UI
        controller.setOnGameStateUpdate(() -> updateUI());
        controller.setOnGameEvent(() -> updateUI());
    }

    private void updateUI() {
        if (gameController == null) return;

        var state = gameController.getCurrentGameState();

        player1ScoreLabel.setText("Игрок 1: " + state.getPlayer1Score());
        player2ScoreLabel.setText("Игрок 2: " + state.getPlayer2Score());
        roundTimeLabel.setText("Время: " + (int)Math.ceil(state.getRoundTime()) + "с");
        roundNumberLabel.setText("Раунд " + state.getRoundNumber());

        // Обновляем индикаторы способностей
        updatePowerUpUI();
    }

    private void updatePowerUpUI() {
        if (gameController == null) return;

        var state = gameController.getCurrentGameState();

        if (state.getPlayer1() != null) {
            updatePlayerPowerUpBar(state.getPlayer1(), player1PowerUpBar);
        }

        if (state.getPlayer2() != null) {
            updatePlayerPowerUpBar(state.getPlayer2(), player2PowerUpBar);
        }

        // Обновляем статус способности для текущего игрока
        boolean canActivate = gameController.canActivatePowerUp();
        if (canActivate) {
            powerUpStatusLabel.setText("Способность: ГОТОВА");
            powerUpStatusLabel.setTextFill(javafx.scene.paint.Color.GOLD);
            powerUpTimerLabel.setText("");
        } else {
            // Проверяем, активна ли способность у локального игрока
            var stateLocal = gameController.getCurrentGameState();
            var player1 = stateLocal.getPlayer1();
            var player2 = stateLocal.getPlayer2();

            // Определяем, кто локальный игрок (упрощенно - считаем игроком 1 если сервер)
            boolean isServer = true; // Нужно получить из GameController

            if (isServer && player1.isPowerUpActive()) {
                powerUpStatusLabel.setText("Способность: АКТИВНА");
                powerUpStatusLabel.setTextFill(javafx.scene.paint.Color.LIME);
                powerUpTimerLabel.setText("Осталось: " +
                        String.format("%.1f", getPowerUpRemainingTime(player1)) + "с");
            } else if (!isServer && player2.isPowerUpActive()) {
                powerUpStatusLabel.setText("Способность: АКТИВНА");
                powerUpStatusLabel.setTextFill(javafx.scene.paint.Color.LIME);
                powerUpTimerLabel.setText("Осталось: " +
                        String.format("%.1f", getPowerUpRemainingTime(player2)) + "с");
            } else {
                powerUpStatusLabel.setText("Способность: ИСПОЛЬЗОВАНА");
                powerUpStatusLabel.setTextFill(javafx.scene.paint.Color.GRAY);
                powerUpTimerLabel.setText("");
            }
        }
    }

    private void updatePlayerPowerUpBar(sumogame.model.Player player, ProgressBar bar) {
        if (player == null || bar == null) return;

        // Для отладки - прогресс бар показывает готовность способности
        if (player.isPowerUpActive()) {
            // Если способность активна, показываем оставшееся время
            bar.setProgress(0.5); // Половина - индикатор активности
            bar.setStyle("-fx-accent: lime;");
        } else if (player.isPowerUpAvailable()) {
            // Если способность доступна
            bar.setProgress(1.0);
            bar.setStyle("-fx-accent: gold;");
        } else {
            // Если способность использована
            bar.setProgress(0.0);
            bar.setStyle("-fx-accent: gray;");
        }
    }

    private double getPowerUpRemainingTime(sumogame.model.Player player) {
        // Это упрощенная реализация
        // В реальном проекте нужно хранить таймер в модели Player
        return 2.5; // Примерное значение
    }

    public GameRenderer getGameRenderer() {
        return gameRenderer;
    }
}