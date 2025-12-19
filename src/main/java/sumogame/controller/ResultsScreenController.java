package sumogame.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sumogame.Main;
import sumogame.model.ArenaType;
import sumogame.model.GameState;

/**
 * Контроллер экрана с итогами матча
 */
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

    private Main main;
    private GameState gameState;
    private boolean isServer; // true = сервер (игрок 1), false = клиент (игрок 2)

    public void setMain(Main main) {
        this.main = main;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        displayResults();
    }

    public void setIsServer(boolean isServer) {
        this.isServer = isServer;
    }

    private void displayResults() {
        if (gameState == null) return;

        // Определяем победителя матча
        int matchWinner = gameState.getMatchWinner();
        String winnerText;
        Color textColor;
        String congratulations = "";

        if (matchWinner == 1) {
            winnerText = "🏆 ПОБЕДИТЕЛЬ: ИГРОК 1 🏆";
            textColor = Color.PINK;
            congratulations = "Игрок 1 победил!";
        } else if (matchWinner == 2) {
            winnerText = "🏆 ПОБЕДИТЕЛЬ: ИГРОК 2 🏆";
            textColor = Color.LIGHTBLUE;
            congratulations = "Игрок 2 победил!";
        } else {
            winnerText = "🤝 НИЧЬЯ! 🤝";
            textColor = Color.GOLD;
            congratulations = "Матч закончился вничью!";
        }

        matchResultLabel.setText(winnerText);
        matchResultLabel.setTextFill(textColor);

        congratulationsLabel.setText(congratulations);
        congratulationsLabel.setTextFill(textColor);

        // Правильно определяем, кто локальный игрок и сравниваем с победителем
        int localPlayerId = isServer ? 1 : 2; // Сервер = игрок 1, Клиент = игрок 2

        System.out.println("Локальный игрок ID: " + localPlayerId);
        System.out.println("Победитель матча ID: " + matchWinner);

        if (matchWinner == 0) {
            // Ничья
            winnerLabel.setText("НИЧЬЯ! ОТЛИЧНАЯ ИГРА! 🤝");
            winnerLabel.setTextFill(Color.GOLD);
        } else if (matchWinner == localPlayerId) {
            // Локальный игрок победил
            winnerLabel.setText("🎉 ПОЗДРАВЛЯЕМ! ВЫ ПОБЕДИЛИ! 🎉");
            winnerLabel.setTextFill(Color.GOLD);
        } else {
            // Локальный игрок проиграл
            winnerLabel.setText("Вы проиграли. Попробуйте еще раз! 💪");
            winnerLabel.setTextFill(Color.LIGHTGRAY);
        }

        // Отображаем счет
        scoreLabel.setText("Финальный счет: " +
                gameState.getPlayer1Score() + " : " + gameState.getPlayer2Score());

        // Отображаем результаты каждого раунда
        displayRoundResults();
    }

    private void displayRoundResults() {
        int[] winners = gameState.getRoundWinners();
        ArenaType[] arenas = ArenaType.values();

        for (int i = 0; i < Math.min(3, winners.length); i++) {
            String roundText = "Раунд " + (i + 1) + " (" + arenas[i].getName() + "): ";
            String resultText;

            switch (winners[i]) {
                case 1:
                    resultText = "Победил Игрок 1";
                    break;
                case 2:
                    resultText = "Победил Игрок 2";
                    break;
                case 0:
                    resultText = "Ничья";
                    break;
                default:
                    resultText = "Не сыгран";
            }

            if (i == 0) round1Result.setText(roundText + resultText);
            if (i == 1) round2Result.setText(roundText + resultText);
            if (i == 2) round3Result.setText(roundText + resultText);
        }
    }

    @FXML
    private void handleReturnToMenu() {
        if (main != null) {
            main.returnToMainMenu();
        }
    }
}