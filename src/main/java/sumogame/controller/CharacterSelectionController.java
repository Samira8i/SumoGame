package sumogame.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import sumogame.Main;
import sumogame.model.CharacterType;

/**
 * Контроллер для экрана выбора персонажа
 */
public class CharacterSelectionController {
    //@FXML указывает, что поле связано с элементом в FXML файле. HBox - горизонтальный контейнер для карточек персонажей.
    @FXML
    private HBox charactersContainer; // Ссылка на контейнер из FXML

    private Main main; // Ссылка на главное приложение
    private CharacterType selectedCharacter; // Выбранный персонаж

    /**
     * Устанавливает ссылку на главное приложение
     */
    public void setMain(Main main) {
        this.main = main;
        initialize(); // Инициализируем когда Main установлен
    }

    /**
     * Инициализация контроллера
     */
    private void initialize() {
        this.selectedCharacter = CharacterType.PINK; // Персонаж по умолчанию
        createCharacterCards(); // Создаем карточки персонажей
    }

    /**
     * Создает карточки для всех типов персонажей
     */
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

    /**
     * Создает одну карточку персонажа
     */
    private VBox createCharacterCard(CharacterType type) {
        VBox card = new VBox(10); // Вертикальный контейнер с отступом 10
        card.setAlignment(javafx.geometry.Pos.CENTER); // Выравнивание по центру
        card.setPadding(new javafx.geometry.Insets(15)); // Внутренние отступы
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 10;");
        card.setUserData(type); // Сохраняем тип персонажа в карточке

        // Круг - визуальное представление персонажа
        Circle characterCircle = new Circle(40); // Круг радиусом 40
        // Устанавливаем цвет в зависимости от типа персонажа
        String colorName = type.name(); // PINK, GREEN, BLUE

        if ("PINK".equals(colorName)) {
            characterCircle.setFill(Color.PINK);
        } else if ("GREEN".equals(colorName)) {
            characterCircle.setFill(Color.GREEN);
        } else if ("BLUE".equals(colorName)) {
            characterCircle.setFill(Color.LIGHTBLUE);
        } else {
            characterCircle.setFill(Color.GRAY);
        }

        // Название персонажа
        Text name = new Text(type.getName());
        name.setStyle("-fx-fill: white; -fx-font-size: 14;");

        // Название способности
        Text ability = new Text("Способность: " + type.getAbilityName());
        ability.setStyle("-fx-fill: lightblue; -fx-font-size: 12;");

        // Описание способности
        Text description = new Text(type.getAbilityDescription());
        description.setStyle("-fx-fill: gray; -fx-font-size: 10;");
        description.setWrappingWidth(150); // Перенос текста

        // Кнопка выбора
        Button selectButton = new Button("Выбрать");
        selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Обработчик нажатия на кнопку
        selectButton.setOnAction(e -> {
            selectedCharacter = type; // Запоминаем выбранного персонажа
            updateSelectionUI(); // Обновляем подсветку
            System.out.println("Выбран персонаж: " + type.getName());
        });

        // Добавляем все элементы в карточку
        card.getChildren().addAll(characterCircle, name, ability, description, selectButton);

        return card;
    }

    /**
     * Обновляет подсветку выбранного персонажа
     */
    private void updateSelectionUI() {
        // Проходим по всем карточкам в контейнере
        for (var node : charactersContainer.getChildren()) {
            VBox card = (VBox) node;
            CharacterType cardType = (CharacterType) card.getUserData();

            // Если это выбранный персонаж - подсвечиваем
            if (cardType == selectedCharacter) {
                card.setStyle("-fx-background-color: #4c4c4c; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 10;");
            } else {
                // Иначе - обычный стиль
                card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 10;");
            }
        }
    }

    /**
     * Обработчик кнопки "Создать игру" (из FXML)
     */
    @FXML //todo: оч странно что где то обработчик вызывается в контроллере а где то  в fxml
    private void handleCreateGame() {
        System.out.println("Нажата кнопка: Создать игру");
        System.out.println("Выбран персонаж: " + selectedCharacter.getName());

        if (main != null) {
            main.startAsServer(selectedCharacter);
        } else {
            showError("Главное приложение не установлено");
        }
    }

    /**
     * Обработчик кнопки "Подключиться" (из FXML)
     */
    @FXML
    private void handleConnectToGame() {
        System.out.println("Нажата кнопка: Подключиться");
        System.out.println("Выбран персонаж: " + selectedCharacter.getName());

        // Создаем диалог для ввода адреса сервера
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Подключение к серверу");
        dialog.setHeaderText("Введите адрес сервера:");
        dialog.setContentText("Адрес:");

        // Показываем диалог и ждем результат
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

    /**
     * Показывает сообщение об ошибке
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}