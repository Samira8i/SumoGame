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

/**
 * Контроллер для экрана выбора персонажа
 */
public class CharacterSelectionController {

    @FXML
    private HBox charactersContainer; // Ссылка на контейнер из FXML
    private Main main; // Ссылка на главное приложение
    private CharacterType selectedCharacter; // Выбранный персонаж

    // Эффекты для дизайна
    private DropShadow cardShadow;
    private DropShadow buttonShadow;
    private DropShadow textShadow;

    public CharacterSelectionController() {
        createEffects();
    }

    private void createEffects() {
        // Тень для карточек
        cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(255, 105, 180, 0.4));
        cardShadow.setRadius(20);
        cardShadow.setOffsetX(0);
        cardShadow.setOffsetY(5);
        cardShadow.setBlurType(BlurType.GAUSSIAN);

        // Тень для кнопок
        buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.rgb(219, 112, 147, 0.6));
        buttonShadow.setRadius(10);
        buttonShadow.setOffsetX(2);
        buttonShadow.setOffsetY(2);

        // Тень для текста
        textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 182, 193, 0.8));
        textShadow.setRadius(3);
        textShadow.setOffsetX(1);
        textShadow.setOffsetY(1);
    }


    public void setMain(Main main) {
        this.main = main;
        initialize(); // Инициализируем когда Main установлен
    }

    private void initialize() {
        this.selectedCharacter = CharacterType.PINK; // Персонаж по умолчанию
        createCharacterCards(); // Создаем карточки персонажей
    }


    private void createCharacterCards() {
        // Очищаем контейнер
        charactersContainer.getChildren().clear();

        // Для каждого типа персонажа создаем карточку
        for (CharacterType type : CharacterType.values()) {
            VBox characterCard = createCharacterCard(type);
            charactersContainer.getChildren().add(characterCard);
        }

        // Обновляем подсветку выбранного персонажа
        updateSelectionUI();
    }


    private VBox createCharacterCard(CharacterType type) {
        VBox card = new VBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPadding(new javafx.geometry.Insets(25, 20, 25, 20));
        card.setEffect(cardShadow);
        card.setUserData(type);

        // Фон карточки с градиентом
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

        // Устанавливаем цвет в зависимости от типа персонажа
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

        // Название персонажа
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

        // Эффект при наведении
        selectButton.setOnMouseEntered(e -> {
            selectButton.setStyle(buttonStyle + " -fx-background-color: linear-gradient(to bottom, #FF1493, #C71585);");
        });

        selectButton.setOnMouseExited(e -> {
            selectButton.setStyle(buttonStyle);
        });

        // Обработчик нажатия на кнопку
        selectButton.setOnAction(e -> {
            selectedCharacter = type;
            updateSelectionUI();
            System.out.println("Выбран персонаж: " + type.getName());
        });

        // Добавляем все элементы в карточку
        card.getChildren().addAll(characterCircle, name, ability, description, selectButton);

        return card;
    }


    private void updateSelectionUI() {
        // Проходим по всем карточкам в контейнере
        for (var node : charactersContainer.getChildren()) {
            VBox card = (VBox) node;
            CharacterType cardType = (CharacterType) card.getUserData();

            // Если это выбранный персонаж - подсвечиваем
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

        if (main != null) {
            main.startAsServer(selectedCharacter);
        } else {
            showError("Главное приложение не установлено");
        }
    }


    @FXML
    private void handleConnectToGame() {
        System.out.println("Нажата кнопка: Подключиться");
        System.out.println("Выбран персонаж: " + selectedCharacter.getName());

        // диалог для ввода адреса сервера
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("🌸 Подключение к серверу");
        dialog.setHeaderText("Введите адрес сервера для подключения:");
        dialog.setContentText("Адрес:");

        dialog.getDialogPane().setStyle("-fx-background-color: #FFF0F5;");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #D8BFD8, #DDA0DD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );

        dialog.showAndWait().ifPresent(address -> {
            if (address != null && !address.trim().isEmpty()) {
                if (main != null) {
                    main.startAsClient(selectedCharacter, address.trim());
                } else {
                    showError("Главное приложение не установлено");
                }
            }
        });
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("🌸 Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Стилизация алерта
        alert.getDialogPane().setStyle("-fx-background-color: #FFF0F5;");
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: linear-gradient(to bottom, #FF69B4, #DB7093); " +
                "-fx-text-fill: white; -fx-font-weight: bold;");

        alert.showAndWait();
    }
}