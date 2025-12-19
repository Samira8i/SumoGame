
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
    private Stage primaryStage; //главное окно приложения
    private GameController gameController;
    private boolean isServerMode;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Сумо Игра");

        showCharacterSelection();
        primaryStage.show();
    }

    private void showCharacterSelection() {
        try {
            //Метод для отображения экрана выбора персонажа. Создает FXMLLoader для загрузки FXML файла
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/character-selection.fxml"));
            //Загружает FXML файл и создает дерево элементов интерфейса
            Parent root = loader.load();
            //Получает контроллер, связанный с FXML файлом, и передает ему ссылку на главный класс.
            CharacterSelectionController controller = loader.getController();
            controller.setMain(this);

            Scene scene = new Scene(root, 800, 600);
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
        // Можно добавить диалоговое окно с ошибкой
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
        // Простой экран с ошибкой
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
    public void showMatchResults(GameState gameState) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/results-screen.fxml"));
            Parent root = loader.load();

            ResultsScreenController controller = loader.getController();
            controller.setMain(this);

            // Передаем GameState для отображения результатов
            if (gameState != null) {
                controller.setGameState(gameState);
                // Передаем информацию о том, кто локальный игрок
                controller.setIsServer(isServerMode);
            }

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);

            // Очищаем gameController
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

    /**
     * Запуск игры в режиме сервера
     */
    public void startAsServer(CharacterType characterType) {
        this.isServerMode = true;
        System.out.println("Запуск в режиме СЕРВЕРА с персонажем: " + characterType.getName());

        try {
            // Загружаем игровой экран
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/game-screen.fxml"));
            Parent root = loader.load();

            // Получаем контроллер игрового экрана
            GameScreenController gameScreenController = loader.getController();

            // Создаем и настраиваем GameController
            gameController = new GameController(true, characterType, null);
            gameController.setMainApp(this); // Передаем ссылку на Main

            // Передаем GameController в GameScreenController
            gameScreenController.setGameController(gameController);

            // Запускаем игру
            gameController.startGame();

            // Передаем GameRenderer в GameController
            gameController.setGameRenderer(gameScreenController.getGameRenderer());

            // Показываем игровой экран
            Scene gameScene = new Scene(root, 800, 600);
            gameScene.setOnKeyPressed(gameScreenController::handleKeyPressed);
            primaryStage.setScene(gameScene);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось запустить сервер: " + e.getMessage());
        }
    }

    /**
     * Запуск в режиме клиента
     */
    public void startAsClient(CharacterType characterType, String serverAddress) {
        this.isServerMode = false;
        System.out.println("Подключение к серверу " + serverAddress + " с персонажем: " + characterType.getName());

        try {
            // Загружаем игровой экран
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sumogame/view/game-screen.fxml"));
            Parent root = loader.load();

            // Получаем контроллер игрового экрана
            GameScreenController gameScreenController = loader.getController();

            // Создаем и настраиваем GameController
            gameController = new GameController(false, characterType, serverAddress);
            gameController.setMainApp(this); // Передаем ссылку на Main

            // Передаем GameController в GameScreenController
            gameScreenController.setGameController(gameController);

            // Запускаем игру
            gameController.startGame();

            // Передаем GameRenderer в GameController
            gameController.setGameRenderer(gameScreenController.getGameRenderer());

            // Показываем игровой экран
            Scene gameScene = new Scene(root, 800, 600);
            gameScene.setOnKeyPressed(gameScreenController::handleKeyPressed);
            primaryStage.setScene(gameScene);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось подключиться к серверу: " + e.getMessage());
        }
    }
}