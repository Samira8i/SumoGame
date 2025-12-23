package sumogame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sumogame.controller.CharacterSelectionController;
import sumogame.controller.GameController;
import sumogame.controller.GameScreenController;
import sumogame.controller.ResultsScreenController;
import sumogame.model.CharacterType;
import sumogame.model.GameState;

public class Main extends Application {
    private Stage primaryStage;
    private GameController gameController;
    private boolean isServerMode;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Сумо Игра");

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);

        showCharacterSelection();
        primaryStage.show();
    }

    private void showCharacterSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/character-selection.fxml"));
            Parent root = loader.load();
            CharacterSelectionController controller = loader.getController();
            controller.setMain(this);

            Scene scene = new Scene(root, 1000, 700);
            primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorScreen(e);
        }
    }

    public void returnToMainMenu() {
        if (gameController != null) {
            gameController.stop();
            gameController = null;
        }
        showCharacterSelection();
    }

    private void showError(String message) {
        System.err.println(message);
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        returnToMainMenu();
    }

    private void showErrorScreen(Exception e) {
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox();
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        javafx.scene.control.Label label = new javafx.scene.control.Label(
                "Ошибка запуска приложения: " + e.getMessage()
        );
        label.setStyle("-fx-text-fill: red; -fx-font-size: 14;");

        javafx.scene.control.Button button = new javafx.scene.control.Button("Выход");
        button.setOnAction(event -> primaryStage.close());

        vbox.getChildren().addAll(label, button);
        primaryStage.setScene(new Scene(vbox, 400, 200));
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showMatchResults(GameState gameState, boolean isLocalPlayer1) {
        try {
            System.out.println("Main.showMatchResults: передаем isLocalPlayer1 = " + isLocalPlayer1);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/results-screen.fxml"));
            Parent root = loader.load();

            ResultsScreenController controller = loader.getController();
            controller.setMain(this);

            if (gameState != null) {
                controller.setIsLocalPlayer1(isLocalPlayer1);
                controller.setGameState(gameState);
            }

            Scene scene = new Scene(root, 1000, 700);
            primaryStage.setScene(scene);

            if (gameController != null) {
                gameController.stop();
                gameController = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось загрузить экран результатов: " + e.getMessage());
            returnToMainMenu();
        }
    }

    public void startAsServer(CharacterType characterType, int port) {
        this.isServerMode = true;
        System.out.println("Запуск сервера на порту " + port + " с персонажем: " + characterType.getName());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/game-screen.fxml"));
            Parent root = loader.load();

            GameScreenController gameScreenController = loader.getController();

            gameController = new GameController(true, characterType, null, port);
            gameController.setMainApp(this);

            gameScreenController.setGameController(gameController);
            gameController.startGame();
            gameController.setGameRenderer(gameScreenController.getGameRenderer());

            Scene gameScene = new Scene(root, 1000, 700);
            gameScene.setOnKeyPressed(gameScreenController::handleKeyPressed);
            primaryStage.setScene(gameScene);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось запустить сервер: " + e.getMessage());
        }
    }

    public void startAsClient(CharacterType characterType, String serverAddress, int port) {
        this.isServerMode = false;
        System.out.println("Подключение к " + serverAddress + ":" + port + " с персонажем: " + characterType.getName());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/game-screen.fxml"));
            Parent root = loader.load();

            GameScreenController gameScreenController = loader.getController();

            gameController = new GameController(false, characterType, serverAddress, port);
            gameController.setMainApp(this);

            gameScreenController.setGameController(gameController);
            gameController.startGame();
            gameController.setGameRenderer(gameScreenController.getGameRenderer());

            Scene gameScene = new Scene(root, 1000, 700);
            gameScene.setOnKeyPressed(gameScreenController::handleKeyPressed);
            primaryStage.setScene(gameScene);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось подключиться к серверу: " + e.getMessage());
        }
    }
}