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
    private AnimationTimer renderLoop;
    private boolean isServer;
    private String serverAddress;
    private Main mainApp;

    // Состояние подключения
    private boolean gameStarted = false;
    private CharacterType opponentCharacter = null;
    private final CharacterType myCharacter;

    // Состояние рендеринга
    private boolean shouldRender = true;

    // Флаг для отправки персонажа
    private boolean characterSent = false;

    public GameController(boolean isServer, CharacterType myCharacter, String serverAddress) {
        this.isServer = isServer;
        this.serverAddress = serverAddress;
        this.myCharacter = myCharacter;

        System.out.println("Режим: " + (isServer ? "сервер" : "клиент"));
        System.out.println("Мой персонаж: " + myCharacter.getName());

        // Инициализация движка
        this.gameEngine = new GameEngine(myCharacter, isServer);
        this.gameEngine.setGameEventListener(this);

        // Инициализация сети
        this.networkManager = new NetworkManager(this, isServer);
        this.networkManager.setMyCharacter(myCharacter);
    }

    public void setMainApp(Main main) {
        this.mainApp = main;
    }

    public void setGameRenderer(GameRenderer renderer) {
        this.gameRenderer = renderer;
        System.out.println("GameRenderer установлен");

        // Запускаем отдельный цикл рендеринга
        startRenderLoop();
    }

    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameRenderer != null && shouldRender) {
                    gameRenderer.render(getCurrentGameState(), isWaitingForOpponent());
                }
            }
        };
        renderLoop.start();
        System.out.println("Цикл рендеринга запущен");
    }

    // Вызывается из UI при нажатии клавиш
    public void handlePlayerInput(String direction) {
        if (!gameStarted || !gameEngine.canProcessInput()) return;

        // Обрабатываем локально
        gameEngine.processPlayerInput(direction, true);

        // Отправляем по сети
        networkManager.sendPlayerMove(direction);
    }

    // Вызывается из UI при активации способности
    public void activatePowerUp() {
        if (!gameStarted || !gameEngine.canActivatePowerUp()) return;

        boolean activated = gameEngine.activatePowerUp();
        if (activated) {
            networkManager.sendPowerUp();
        }
    }

    // Обработка сетевого ввода (от противника)
    public void handleNetworkInput(String direction) {
        if (!gameStarted || !gameEngine.canProcessInput()) return;

        gameEngine.processPlayerInput(direction, false);
    }

    // Обработка сетевой активации способности
    public void handleNetworkPowerUp() {
        if (!gameStarted) return;

        gameEngine.processOpponentPowerUp();
    }

    @Override
    public void onGameStateUpdated(GameState state) {
        // Больше не вызываем коллбэк, GameScreenController обновляет UI через свой AnimationTimer
        // UI обновляется в GameScreenController.updateUI() через собственный таймер
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
                    System.out.println("GameController: Победитель матча = " + getCurrentGameState().getMatchWinner());
                    System.out.println("GameController: Сервер? " + isServer);
                    break;
                case "SHOW_RESULTS":
                    System.out.println("📊 Показываем результаты...");
                    // Показываем экран результатов через Main
                    if (mainApp != null) {
                        GameState currentState = getCurrentGameState();
                        System.out.println("GameController.SHOW_RESULTS: isServer = " + isServer);
                        System.out.println("GameController.SHOW_RESULTS: Передаем isLocalPlayer1 = " + isServer);

                        boolean isLocalPlayer1 = isServer; // Сервер = Player1, Клиент = Player2
                        mainApp.showMatchResults(currentState, isLocalPlayer1);
                    }
                    break;
                case "PLAYER_DISCONNECTED":
                    System.out.println("Противник отключился!");
                    break;
            }
            // Больше не вызываем коллбэк onGameEvent
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
        shouldRender = false;

        if (renderLoop != null) {
            renderLoop.stop();
            System.out.println("Цикл рендеринга остановлен");
        }

        if (gameLoop != null) {
            gameLoop.stop();
            System.out.println("Игровой цикл остановлен");
        }

        if (networkManager != null) {
            networkManager.disconnect();
            System.out.println("Сетевое соединение закрыто");
        }
    }

    // Метод для обновления персонажа противника
    public void updateOpponentCharacter(CharacterType opponentCharacter) {
        this.opponentCharacter = opponentCharacter;
        System.out.println("GameController: Получен персонаж противника: " + opponentCharacter.getName());

        if (gameEngine != null) {
            gameEngine.updateOpponentCharacter(opponentCharacter);
        }

        // Проверяем, можно ли начать игру
        checkIfGameCanStart();
    }

    // Метод для обработки подключения противника
    public void onOpponentConnected() {
        System.out.println("GameController: Противник подключился!");
        // При подключении отправляем свой персонаж, если еще не отправляли
        if (!characterSent && myCharacter != null) {
            System.out.println("Отправляю мой персонаж после подключения: " + myCharacter.name());
            networkManager.sendPlayerJoin(myCharacter.name());
            characterSent = true;
        }
    }

    private void checkIfGameCanStart() {
        System.out.println("GameController: Проверка готовности игры...");
        System.out.println("  Противник: " + (opponentCharacter != null ? opponentCharacter.getName() : "неизвестен"));
        System.out.println("  Игра начата: " + gameStarted);

        // Игра может начаться, если мы знаем персонаж противника
        if (opponentCharacter != null && !gameStarted) {
            System.out.println("GameController: Все готово к запуску игры!");

            // Небольшая задержка для синхронизации
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(() -> {
                        if (!gameStarted) {
                            startActualGame();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            System.out.println("GameController: Игра еще не может начаться");
        }
    }

    private void startActualGame() {
        if (gameStarted) {
            System.out.println("GameController: Игра уже начата!");
            return;
        }

        gameStarted = true;
        System.out.println("=== ИГРА НАЧИНАЕТСЯ ===");
        System.out.println("Режим: " + (isServer ? "СЕРВЕР" : "КЛИЕНТ"));
        System.out.println("Мой персонаж: " + myCharacter.getName());
        System.out.println("Персонаж противника: " + opponentCharacter.getName());

        // Запускаем игру в GameEngine
        gameEngine.startGame();

        // Запускаем игровой цикл
        startGameLoop();
    }

    public void startGame() {
        System.out.println("GameController: запуск сетевого режима, режим: " + (isServer ? "СЕРВЕР" : "КЛИЕНТ"));

        // Начинаем сетевое соединение
        if (isServer) {
            System.out.println("Сервер запущен, ожидание подключения клиента...");
            networkManager.startNetwork();
        } else {
            String address = serverAddress != null ? serverAddress : "localhost";
            System.out.println("Подключение к серверу: " + address);
            networkManager.connect(address);
        }

        // Отправляем свой выбор персонажа
        if (!isServer) {
            System.out.println("GameController (клиент): Отправляю мой персонаж: " + myCharacter.name());
            networkManager.sendPlayerJoin(myCharacter.name());
            characterSent = true;
        }
    }

    public boolean canActivatePowerUp() {
        return gameStarted && gameEngine.canActivatePowerUp();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isWaitingForOpponent() {
        return !gameStarted;
    }

    public String getServerAddress() {
        return serverAddress != null ? serverAddress : "localhost";
    }
}