package sumogame.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import sumogame.Main;
import sumogame.model.CharacterType;

public class CharacterSelectionController {

    @FXML
    private HBox charactersContainer;
    private Main main;
    private CharacterType selectedCharacter;

    private DropShadow cardShadow;
    private DropShadow buttonShadow;
    private DropShadow textShadow;

    public CharacterSelectionController() {
        createEffects();
    }

    private void createEffects() {
        cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(255, 105, 180, 0.4));
        cardShadow.setRadius(20);
        cardShadow.setOffsetX(0);
        cardShadow.setOffsetY(5);
        cardShadow.setBlurType(BlurType.GAUSSIAN);

        buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.rgb(219, 112, 147, 0.6));
        buttonShadow.setRadius(10);
        buttonShadow.setOffsetX(2);
        buttonShadow.setOffsetY(2);

        textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 182, 193, 0.8));
        textShadow.setRadius(3);
        textShadow.setOffsetX(1);
        textShadow.setOffsetY(1);
    }

    public void setMain(Main main) {
        this.main = main;
        initialize();
    }

    private void initialize() {
        this.selectedCharacter = CharacterType.PINK;
        createCharacterCards();
    }

    private void createCharacterCards() {
        charactersContainer.getChildren().clear();

        for (CharacterType type : CharacterType.values()) {
            VBox characterCard = createCharacterCard(type);
            charactersContainer.getChildren().add(characterCard);
        }

        updateSelectionUI();
    }

    private VBox createCharacterCard(CharacterType type) {
        VBox card = new VBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPadding(new javafx.geometry.Insets(25, 20, 25, 20));
        card.setEffect(cardShadow);
        card.setUserData(type);

        String cardStyle = "-fx-background-color: linear-gradient(to bottom, #FFF0F5, #FFE4E1); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #FFB6C1; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 18;";
        card.setStyle(cardStyle);

        Circle characterCircle = new Circle(50);

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        innerShadow.setRadius(15);
        innerShadow.setOffsetX(2);
        innerShadow.setOffsetY(2);
        characterCircle.setEffect(innerShadow);

        switch (type) {
            case PINK:
                characterCircle.setFill(Color.web("#FFB6C1"));
                break;
            case GREEN:
                characterCircle.setFill(Color.web("#98FB98"));
                break;
            case BLUE:
                characterCircle.setFill(Color.web("#ADD8E6"));
                break;
        }

        Text name = new Text(type.getName());
        name.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));
        name.setFill(Color.web("#DB7093"));
        name.setEffect(textShadow);

        Text ability = new Text("💫 " + type.getAbilityName());
        ability.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
        ability.setFill(Color.web("#C71585"));

        Text description = new Text(type.getAbilityDescription());
        description.setFont(Font.font("Arial", 12));
        description.setFill(Color.web("#8B6969"));
        description.setWrappingWidth(180);

        Button selectButton = new Button("✨ Выбрать");
        selectButton.setPrefWidth(120);
        selectButton.setPrefHeight(40);
        selectButton.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
        selectButton.setEffect(buttonShadow);

        String buttonStyle = "-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: #FFC0CB; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 13;";
        selectButton.setStyle(buttonStyle);

        selectButton.setOnMouseEntered(e -> {
            selectButton.setStyle(buttonStyle + " -fx-background-color: linear-gradient(to bottom, #FF1493, #C71585);");
        });

        selectButton.setOnMouseExited(e -> {
            selectButton.setStyle(buttonStyle);
        });

        selectButton.setOnAction(e -> {
            selectedCharacter = type;
            updateSelectionUI();
            System.out.println("Выбран персонаж: " + type.getName());
        });

        card.getChildren().addAll(characterCircle, name, ability, description, selectButton);
        return card;
    }

    private void updateSelectionUI() {
        for (var node : charactersContainer.getChildren()) {
            VBox card = (VBox) node;
            CharacterType cardType = (CharacterType) card.getUserData();

            if (cardType == selectedCharacter) {
                String selectedStyle = "-fx-background-color: linear-gradient(to bottom, #FFE4E9, #FFD1DC); " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #FF69B4; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 18; " +
                        "-fx-effect: dropshadow(gaussian, #FF69B4, 30, 0.5, 0, 5);";
                card.setStyle(selectedStyle);
            } else {
                String normalStyle = "-fx-background-color: linear-gradient(to bottom, #FFF0F5, #FFE4E1); " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #FFB6C1; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 18; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,105,180,0.4), 20, 0, 0, 5);";
                card.setStyle(normalStyle);
            }
        }
    }

    @FXML
    private void handleCreateGame() {
        System.out.println("Нажата кнопка: Создать игру");
        System.out.println("Выбран персонаж: " + selectedCharacter.getName());

        // Диалог для выбора порта
        TextInputDialog portDialog = new TextInputDialog("8080");
        portDialog.setTitle("🌸 Настройки сервера");
        portDialog.setHeaderText("Введите порт для сервера:");
        portDialog.setContentText("Порт (1024-65535):");

        portDialog.getDialogPane().setStyle("-fx-background-color: #FFF0F5;");
        portDialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );
        portDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #D8BFD8, #DDA0DD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );

        portDialog.showAndWait().ifPresent(portStr -> {
            if (portStr != null && !portStr.trim().isEmpty()) {
                try {
                    int port = Integer.parseInt(portStr.trim());
                    if (port < 1024 || port > 65535) {
                        showError("Порт должен быть в диапазоне 1024-65535");
                        return;
                    }

                    if (main != null) {
                        main.startAsServer(selectedCharacter, port);
                    }
                } catch (NumberFormatException e) {
                    showError("Некорректный номер порта");
                }
            }
        });
    }

    @FXML
    private void handleConnectToGame() {
        System.out.println("Нажата кнопка: Подключиться");
        System.out.println("Выбран персонаж: " + selectedCharacter.getName());

        // Диалог для ввода адреса и порта
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        javafx.scene.control.TextField addressField = new javafx.scene.control.TextField("localhost");
        javafx.scene.control.TextField portField = new javafx.scene.control.TextField("8080");

        grid.add(new javafx.scene.control.Label("Адрес сервера:"), 0, 0);
        grid.add(addressField, 1, 0);
        grid.add(new javafx.scene.control.Label("Порт:"), 0, 1);
        grid.add(portField, 1, 1);

        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("🌸 Подключение к серверу");
        dialog.setHeaderText("Введите параметры подключения:");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.getDialogPane().setStyle("-fx-background-color: #FFF0F5;");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #D8BFD8, #DDA0DD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String address = addressField.getText().trim();
                String portStr = portField.getText().trim();

                if (address.isEmpty()) {
                    showError("Адрес сервера не может быть пустым");
                    return;
                }

                try {
                    int port = Integer.parseInt(portStr);
                    if (port < 1024 || port > 65535) {
                        showError("Порт должен быть в диапазоне 1024-65535");
                        return;
                    }

                    if (main != null) {
                        main.startAsClient(selectedCharacter, address, port);
                    }
                } catch (NumberFormatException e) {
                    showError("Некорректный номер порта");
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("🌸 Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.getDialogPane().setStyle("-fx-background-color: #FFF0F5;");
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                "-fx-text-fill: white; -fx-font-weight: bold;");

        alert.showAndWait();
    }
}