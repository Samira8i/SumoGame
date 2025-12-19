package sumogame.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import sumogame.Main;
import sumogame.engine.GameEngine;
import sumogame.engine.GameEventListener;
import sumogame.model.*;
import sumogame.network.NetworkManager;
import sumogame.view.GameRenderer;

public class GameController implements GameEventListener {
    private GameEngine gameEngine;
    private NetworkManager networkManager;
    private GameRenderer gameRenderer;
    private AnimationTimer gameLoop;
    private boolean isServer;
    private String serverAddress;
    private Main mainApp;

    // Колбэки для UI
    private Runnable onGameStateUpdate;
    private Runnable onGameEvent;
    private Runnable onShowResults;

    public GameController(boolean isServer, CharacterType myCharacter, String serverAddress) {
        this.isServer = isServer;
        this.serverAddress = serverAddress;

        // Инициализация движка
        this.gameEngine = new GameEngine(myCharacter, isServer);
        this.gameEngine.setGameEventListener(this);

        // Инициализация сети
        this.networkManager = new NetworkManager(this);
    }

    public void setMainApp(Main main) {
        this.mainApp = main;
    }

    public void setGameRenderer(GameRenderer renderer) {
        this.gameRenderer = renderer;
        System.out.println("GameRenderer установлен");
    }

    // Вызывается из UI при нажатии клавиш
    public void handlePlayerInput(String direction) {
        if (gameEngine.canProcessInput()) {
            // Обрабатываем локально
            gameEngine.processPlayerInput(direction, true);

            // Отправляем по сети
            networkManager.sendPlayerMove(direction);
        }
    }

    // Вызывается из UI при активации способности
    public void activatePowerUp() {
        if (gameEngine.canActivatePowerUp()) {
            boolean activated = gameEngine.activatePowerUp();
            if (activated) {
                networkManager.sendPowerUp();
            }
        }
    }

    // Обработка сетевого ввода (от противника)
    public void handleNetworkInput(String direction) {
        if (gameEngine.canProcessInput()) {
            gameEngine.processPlayerInput(direction, false);
        }
    }

    // Обработка сетевой активации способности
    public void handleNetworkPowerUp() {
        gameEngine.processOpponentPowerUp();
    }

    @Override
    public void onGameStateUpdated(GameState state) {
        Platform.runLater(() -> {
            // Рендерим игру
            if (gameRenderer != null) {
                gameRenderer.render(state);
            }

            // Обновляем UI
            if (onGameStateUpdate != null) {
                onGameStateUpdate.run();
            }
        });
    }

    @Override
    public void onGameEvent(String eventType, String data) {
        Platform.runLater(() -> {
            System.out.println("Событие игры: " + eventType + " - " + data);

            switch (eventType) {
                case "GAME_STARTED":
                    System.out.println("✅ Игра началась!");
                    break;
                case "ROUND_STARTED":
                    System.out.println("🚀 " + data);
                    break;
                case "ROUND_ENDED":
                    System.out.println("🏁 " + data);
                    break;
                case "MATCH_FINISHED":
                    System.out.println("🎮 " + data);
                    break;
                case "SHOW_RESULTS":
                    System.out.println("📊 Показываем результаты...");
                    // Показываем экран результатов
                    if (mainApp != null) {
                        mainApp.showMatchResults(getCurrentGameState());
                    } else if (onShowResults != null) {
                        onShowResults.run();
                    }
                    break;
                case "PLAYER_DISCONNECTED":
                    System.out.println("Противник отключился!");
                    break;
            }

            if (onGameEvent != null) {
                onGameEvent.run();
            }
        });
    }

    public GameState getCurrentGameState() {
        return gameEngine.getGameState();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                // Обновляем игровую логику
                gameEngine.update(deltaTime);
            }
        };
        gameLoop.start();
        System.out.println("Игровой цикл запущен");
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
            System.out.println("Игровой цикл остановлен");
        }
        if (networkManager != null) {
            networkManager.disconnect();
            System.out.println("Сетевое соединение закрыто");
        }
    }

    // Установка колбэков для UI
    public void setOnGameStateUpdate(Runnable callback) {
        this.onGameStateUpdate = callback;
    }

    public void setOnGameEvent(Runnable callback) {
        this.onGameEvent = callback;
    }

    public void setOnShowResults(Runnable callback) {
        this.onShowResults = callback;
    }
    public void updateOpponentCharacter(CharacterType opponentCharacter) {
        if (gameEngine != null) {
            gameEngine.updateOpponentCharacter(opponentCharacter);
        }
    }

    public void startGame() {
        System.out.println("GameController: запуск игры, режим: " + (isServer ? "СЕРВЕР" : "КЛИЕНТ"));

        // Начинаем сетевое соединение
        if (isServer) {
            networkManager.startServer();
            System.out.println("Сервер запущен, ожидание подключения...");
        } else {
            String address = serverAddress != null ? serverAddress : "localhost";
            System.out.println("Подключение к серверу: " + address);
            networkManager.connectToServer(address);
        }

        // Отправляем свой выбор персонажа
        // Получаем выбранный персонаж из GameEngine (локальный персонаж)
        CharacterType myCharacter = gameEngine.getGameState().getPlayer1().getType();
        if (!isServer) {
            // Для клиента локальный игрок - player2
            myCharacter = gameEngine.getGameState().getPlayer2().getType();
        }

        System.out.println("Мой персонаж: " + myCharacter.getName());
        networkManager.sendPlayerJoin(myCharacter);

        // Запускаем игровой цикл
        startGameLoop();
    }

    // Геттеры для UI
    public boolean canProcessInput() {
        return gameEngine.canProcessInput();
    }

    public boolean canActivatePowerUp() {
        return gameEngine.canActivatePowerUp();
    }
}