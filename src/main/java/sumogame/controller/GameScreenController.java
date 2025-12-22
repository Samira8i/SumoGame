package sumogame.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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

    private AnimationTimer uiUpdateTimer;
    private int lastDisplayedTime = -1;

    @FXML
    public void initialize() {
        System.out.println("GameScreenController: Инициализация");

        // Создаем рендерер
        gameRenderer = new GameRenderer(gameCanvas);

        // Фиксируем размер канваса
        gameCanvas.setWidth(900);
        gameCanvas.setHeight(480);

        // Устанавливаем начальные значения
        Platform.runLater(() -> {
            roundTimeLabel.setText("⏱️ 60с");
            roundTimeLabel.setTextFill(Color.web("#FF69B4"));
            roundNumberLabel.setText("🌸 Раунд 1");
            player1ScoreLabel.setText("Игрок 1: 0");
            player2ScoreLabel.setText("Игрок 2: 0");
            powerUpStatusLabel.setText("✨ Способность: ГОТОВА");
        });

        // Запускаем таймер обновления UI
        startUIUpdateTimer();
    }

    private void startUIUpdateTimer() {
        uiUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateUI();
            }
        };
        uiUpdateTimer.start();
    }

    private void updateUI() {
        if (gameController == null) return;

        try {
            if (gameController.isGameStarted()) {
                var state = gameController.getCurrentGameState();
                if (state == null) return;

                Platform.runLater(() -> {
                    // Обновляем таймер
                    int timeLeft = (int) Math.ceil(state.getRoundTime());
                    if (timeLeft != lastDisplayedTime) {
                        roundTimeLabel.setText("⏱️ " + timeLeft + "с");

                        // Меняем цвет таймера
                        if (timeLeft <= 10) {
                            roundTimeLabel.setTextFill(Color.RED);
                            roundTimeLabel.setStyle(
                                    "-fx-text-fill: red; -fx-font-size: 20; " +
                                            "-fx-font-weight: bold; -fx-padding: 4 8; " +
                                            "-fx-background-color: white; -fx-background-radius: 8; " +
                                            "-fx-border-color: #FF4500; -fx-border-width: 2; " +
                                            "-fx-border-radius: 6;"
                            );
                        } else if (timeLeft <= 30) {
                            roundTimeLabel.setTextFill(Color.ORANGE);
                            roundTimeLabel.setStyle(
                                    "-fx-text-fill: orange; -fx-font-size: 20; " +
                                            "-fx-font-weight: bold; -fx-padding: 4 8; " +
                                            "-fx-background-color: white; -fx-background-radius: 8; " +
                                            "-fx-border-color: #FFA500; -fx-border-width: 2; " +
                                            "-fx-border-radius: 6;"
                            );
                        } else {
                            roundTimeLabel.setTextFill(Color.web("#FF69B4"));
                            roundTimeLabel.setStyle(
                                    "-fx-text-fill: #FF69B4; -fx-font-size: 20; " +
                                            "-fx-font-weight: bold; -fx-padding: 4 8; " +
                                            "-fx-background-color: white; -fx-background-radius: 8; " +
                                            "-fx-border-color: #FFB6C1; -fx-border-width: 2; " +
                                            "-fx-border-radius: 6;"
                            );
                        }
                        lastDisplayedTime = timeLeft;
                    }

                    // Обновляем остальной UI
                    roundNumberLabel.setText("🌸 Раунд " + state.getRoundNumber());
                    player1ScoreLabel.setText("Игрок 1: " + state.getPlayer1Score());
                    player2ScoreLabel.setText("Игрок 2: " + state.getPlayer2Score());

                    // Обновляем индикаторы способностей
                    updatePowerUpUI(state);
                });
            }
        } catch (Exception e) {
            System.err.println("Ошибка обновления UI: " + e.getMessage());
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent event) {
        if (gameController == null || !gameController.isGameStarted()) return;

        String direction = null;

        switch (event.getCode()) {
            case W: case UP: direction = "UP"; break;
            case S: case DOWN: direction = "DOWN"; break;
            case A: case LEFT: direction = "LEFT"; break;
            case D: case RIGHT: direction = "RIGHT"; break;
            case SPACE:
                if (gameController.canActivatePowerUp()) {
                    gameController.activatePowerUp();
                }
                break;
        }

        if (direction != null) {
            gameController.handlePlayerInput(direction);
        }
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
        // Больше не устанавливаем коллбэки
        requestFocus();
    }

    private void updatePowerUpUI(sumogame.model.GameState state) {
        if (state == null) return;

        // Обновляем прогресс-бары
        if (state.getPlayer1() != null) {
            updatePowerUpBar(player1PowerUpBar, state.getPlayer1());
        }
        if (state.getPlayer2() != null) {
            updatePowerUpBar(player2PowerUpBar, state.getPlayer2());
        }

        // Обновляем статус способности
        if (gameController != null && gameController.canActivatePowerUp()) {
            powerUpStatusLabel.setText("✨ Способность: ГОТОВА");
            powerUpStatusLabel.setTextFill(Color.web("#FF69B4"));
            powerUpTimerLabel.setText("");
        } else if (state.getPlayer1() != null && state.getPlayer1().isPowerUpActive()) {
            powerUpStatusLabel.setText("✨ Способность: АКТИВНА");
            powerUpStatusLabel.setTextFill(Color.web("#32CD32"));
            powerUpTimerLabel.setText("Осталось: 2.5с");
        } else if (state.getPlayer2() != null && state.getPlayer2().isPowerUpActive()) {
            powerUpStatusLabel.setText("✨ Способность: АКТИВНА");
            powerUpStatusLabel.setTextFill(Color.web("#32CD32"));
            powerUpTimerLabel.setText("Осталось: 2.5с");
        } else {
            powerUpStatusLabel.setText("✨ Способность: ИСПОЛЬЗОВАНА");
            powerUpStatusLabel.setTextFill(Color.web("#8B6969"));
            powerUpTimerLabel.setText("");
        }
    }

    private void updatePowerUpBar(ProgressBar bar, sumogame.model.Player player) {
        if (player.isPowerUpActive()) {
            bar.setProgress(0.5);
            bar.setStyle("-fx-accent: #32CD32; -fx-background-radius: 6;");
        } else if (player.isPowerUpAvailable()) {
            bar.setProgress(1.0);
            bar.setStyle("-fx-accent: #FFD700; -fx-background-radius: 6;");
        } else {
            bar.setProgress(0.0);
            bar.setStyle("-fx-accent: #CCCCCC; -fx-background-radius: 6;");
        }
    }

    public GameRenderer getGameRenderer() {
        return gameRenderer;
    }

    public void requestFocus() {
        if (gameContainer != null) {
            gameContainer.requestFocus();
        }
    }

}