package sumogame.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sumogame.Main;
import sumogame.model.ArenaType;
import sumogame.model.GameState;

public class ResultsScreenController {

    @FXML private VBox resultsContainer;
    @FXML private Label matchResultLabel;
    @FXML private Label scoreLabel;
    @FXML private Label round1Result;
    @FXML private Label round2Result;
    @FXML private Label round3Result;
    @FXML private Button returnToMenuButton;

    @FXML private Label congratulationsLabel;
    @FXML private Label winnerLabel;
    @FXML private Label subtitleLabel;

    private Main main;
    private GameState gameState;
    private boolean isLocalPlayer1 = false;
    private boolean parametersSet = false;

    // Цвета в розовой палитре
    private final Color PINK_PRIMARY = Color.web("#FF69B4");
    private final Color PINK_LIGHT = Color.web("#FFB6C1");
    private final Color PINK_DARK = Color.web("#DB7093");
    private final Color GOLD = Color.web("#FFD700");
    private final Color WHITE_SMOKE = Color.web("#F5F5F5");
    private final Color LAVENDER = Color.web("#E6E6FA");

    private DropShadow textShadow;
    private DropShadow buttonShadow;

    @FXML
    public void initialize() {
        createEffects();
        styleElements();
    }

    private void createEffects() {
        textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(219, 112, 147, 0.6));
        textShadow.setRadius(10);
        textShadow.setOffsetX(2);
        textShadow.setOffsetY(2);
        textShadow.setBlurType(BlurType.GAUSSIAN);

        buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.rgb(255, 105, 180, 0.5));
        buttonShadow.setRadius(15);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(5);
    }

    private void styleElements() {
        // Фон контейнера с градиентом
        String backgroundStyle = "-fx-background-color: linear-gradient(to bottom, #FFF0F5, #FFE4E1);";
        resultsContainer.setStyle(backgroundStyle);

        // Стиль заголовка
        matchResultLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 36));
        matchResultLabel.setEffect(textShadow);

        // Стиль поздравления
        congratulationsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        // Стиль победителя
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Стиль подзаголовка
        subtitleLabel.setText("🌸 Итоги матча 🌸");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        subtitleLabel.setTextFill(PINK_DARK);

        // Стиль кнопки
        String buttonStyle = "-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 15 40; " +
                "-fx-background-radius: 25; " +
                "-fx-border-color: #FFB6C1; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 22;";
        returnToMenuButton.setStyle(buttonStyle);
        returnToMenuButton.setEffect(buttonShadow);

        // Эффекты при наведении
        returnToMenuButton.setOnMouseEntered(e -> {
            returnToMenuButton.setStyle(buttonStyle +
                    " -fx-background-color: linear-gradient(to bottom, #FF1493, #C71585);");
        });

        returnToMenuButton.setOnMouseExited(e -> {
            returnToMenuButton.setStyle(buttonStyle);
        });

        // Стиль результатов раундов
        String roundStyle = "-fx-font-size: 16px; -fx-font-weight: bold;";
        round1Result.setStyle(roundStyle);
        round2Result.setStyle(roundStyle);
        round3Result.setStyle(roundStyle);

        // Стиль счета
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        scoreLabel.setTextFill(PINK_DARK);
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        checkAndDisplayResults();
    }

    public void setIsLocalPlayer1(boolean isLocalPlayer1) {
        this.isLocalPlayer1 = isLocalPlayer1;
        checkAndDisplayResults();
    }

    private void checkAndDisplayResults() {
        if (gameState != null && !parametersSet) {
            parametersSet = true;
            displayResults();
        }
    }

    private void displayResults() {
        if (gameState == null) return;

        int matchWinner = gameState.getMatchWinner();
        String winnerText;
        Color textColor;
        String congratulations = "";

        // Определение победителя матча
        if (matchWinner == 1) {
            winnerText = "🌸 ПОБЕДИТЕЛЬ: ИГРОК 1 🌸";
            textColor = PINK_PRIMARY;
            congratulations = "Игрок 1 одержал победу!";
        } else if (matchWinner == 2) {
            winnerText = "🌸 ПОБЕДИТЕЛЬ: ИГРОК 2 🌸";
            textColor = PINK_PRIMARY;
            congratulations = "Игрок 2 одержал победу!";
        } else {
            winnerText = "🤝 НИЧЬЯ! 🤝";
            textColor = GOLD;
            congratulations = "Матч завершился вничью!";
        }

        matchResultLabel.setText(winnerText);
        matchResultLabel.setTextFill(textColor);

        congratulationsLabel.setText(congratulations);
        congratulationsLabel.setTextFill(textColor);

        // Определяем результат для локального игрока
        int localPlayerId = isLocalPlayer1 ? 1 : 2;

        if (matchWinner == 0) {
            // Ничья
            winnerLabel.setText("Великолепная игра! Оба достойны победы! 🎌");
            winnerLabel.setTextFill(GOLD);
            applySparkleEffect(winnerLabel);
        } else if (matchWinner == localPlayerId) {
            // Победа локального игрока
            winnerLabel.setText("🎉 ПОЗДРАВЛЯЕМ! ВЫ ПОБЕДИЛИ! 🎉");
            winnerLabel.setTextFill(GOLD);
            applyWinnerEffects();
        } else {
            // Поражение локального игрока
            winnerLabel.setText("Вы достойно сражались! Попробуйте еще раз! 💪");
            winnerLabel.setTextFill(LAVENDER);
        }

        // Отображаем счет
        scoreLabel.setText("Финальный счет: " +
                gameState.getPlayer1Score() + " : " + gameState.getPlayer2Score());

        // Отображаем результаты раундов
        displayRoundResults();
    }

    private void applyWinnerEffects() {
        // Градиентный текст для победителя
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, GOLD),
                new Stop(0.5, Color.WHITE),
                new Stop(1, GOLD)
        );
        winnerLabel.setTextFill(gradient);

        // Эффект свечения
        DropShadow glow = new DropShadow();
        glow.setColor(GOLD);
        glow.setRadius(20);
        glow.setSpread(0.5);
        winnerLabel.setEffect(glow);
    }

    private void applySparkleEffect(Label label) {
        // Легкое мерцание для ничьей
        InnerShadow innerGlow = new InnerShadow();
        innerGlow.setColor(GOLD.deriveColor(0, 1, 1, 0.3));
        innerGlow.setRadius(10);
        innerGlow.setOffsetX(0);
        innerGlow.setOffsetY(0);
        label.setEffect(innerGlow);
    }

    private void displayRoundResults() {
        int[] winners = gameState.getRoundWinners();
        ArenaType[] arenas = ArenaType.values();

        for (int i = 0; i < Math.min(3, winners.length); i++) {
            String roundText = "🌸 Раунд " + (i + 1) + " (" + arenas[i].getName() + "): ";
            String resultText;
            Color resultColor;

            switch (winners[i]) {
                case 1:
                    resultText = "Победил Игрок 1";
                    resultColor = PINK_PRIMARY;
                    break;
                case 2:
                    resultText = "Победил Игрок 2";
                    resultColor = PINK_PRIMARY;
                    break;
                case 0:
                    resultText = "Ничья";
                    resultColor = GOLD;
                    break;
                default:
                    resultText = "Не сыгран";
                    resultColor = Color.GRAY;
            }

            String fullText = roundText + resultText;

            if (i == 0) {
                round1Result.setText(fullText);
                round1Result.setTextFill(resultColor);
            }
            if (i == 1) {
                round2Result.setText(fullText);
                round2Result.setTextFill(resultColor);
            }
            if (i == 2) {
                round3Result.setText(fullText);
                round3Result.setTextFill(resultColor);
            }
        }
    }

    @FXML
    private void handleReturnToMenu() {
        if (main != null) {
            main.returnToMainMenu();
        }
    }
}