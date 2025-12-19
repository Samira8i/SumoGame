package sumogame.engine;

import javafx.application.Platform;
import sumogame.model.*;

public class GameEngine {
    private GameState gameState;
    private GameEventListener listener;
    private boolean isServer;
    private CharacterType localCharacter;
    private double roundTimer;
    private boolean roundEnded;

    // Флаг для предотвращения повторного завершения раунда
    private boolean roundCompletionInProgress = false;

    public GameEngine(CharacterType localCharacter, boolean isServer) {
        this.localCharacter = localCharacter;
        this.isServer = isServer;
        this.roundEnded = false;
        this.roundCompletionInProgress = false;

        initializeGame();
    }

    private void initializeGame() {
        int arenaWidth = GameConfig.ARENA_WIDTH;
        int arenaHeight = GameConfig.ARENA_HEIGHT;

        // Для сервера: сервер = игрок 1 со своим выбором, клиент = игрок 2 (розовый по умолчанию)
        // Для клиента: сервер = игрок 1 (розовый по умолчанию), клиент = игрок 2 со своим выбором
        // Проблема: мы не знаем выбор противника до получения сетевого сообщения

        // Временное решение: использовать выбранный персонаж для локального игрока
        // Противнику пока назначаем розового, потом обновим через сеть
        CharacterType player1Type, player2Type;

        if (isServer) {
            // Сервер - игрок 1 с выбранным персонажем
            player1Type = localCharacter;
            // Клиента пока не знаем - розовый по умолчанию
            player2Type = CharacterType.PINK;
        } else {
            // Клиент - сервер розовый (пока не знаем)
            player1Type = CharacterType.PINK;
            // Клиент - игрок 2 с выбранным персонажем
            player2Type = localCharacter;
        }

        Player player1 = new Player(1, player1Type, arenaWidth * 0.25, arenaHeight / 2);
        Player player2 = new Player(2, player2Type, arenaWidth * 0.75, arenaHeight / 2);

        this.gameState = new GameState();
        gameState.setPlayer1(player1);
        gameState.setPlayer2(player2);
        gameState.setArenaWidth(arenaWidth);
        gameState.setArenaHeight(arenaHeight);
        gameState.setRoundTime(GameConfig.ROUND_DURATION);

        // Устанавливаем арену для первого раунда
        gameState.setCurrentArena(ArenaType.getByRoundNumber(1));

        this.roundTimer = GameConfig.ROUND_DURATION;
        gameState.setGameActive(true);
        roundEnded = false;

        if (listener != null) {
            listener.onGameEvent("GAME_STARTED", "");
            listener.onGameEvent("ROUND_STARTED", "Раунд 1 из 3 - " + gameState.getCurrentArena().getName());
        }

        System.out.println("=== ИГРА НАЧАЛАСЬ ===");
        System.out.println("Режим: " + (isServer ? "СЕРВЕР" : "КЛИЕНТ"));
        System.out.println("Игрок 1: " + player1Type.getName());
        System.out.println("Игрок 2: " + player2Type.getName());
        System.out.println("Раунд: 1/3");
        System.out.println("Арена: " + gameState.getCurrentArena().getName());
        System.out.println("Счет: 0 - 0");
    }

    // Добавим метод для обновления персонажа противника
    public void updateOpponentCharacter(CharacterType opponentCharacter) {
        if (isServer) {
            // Сервер обновляет персонаж игрока 2 (клиента)
            gameState.getPlayer2().setType(opponentCharacter);
            System.out.println("Клиент выбрал: " + opponentCharacter.getName());
        } else {
            // Клиент обновляет персонаж игрока 1 (сервера)
            gameState.getPlayer1().setType(opponentCharacter);
            System.out.println("Сервер выбрал: " + opponentCharacter.getName());
        }
        notifyStateUpdate();
    }

    public void processPlayerInput(String direction, boolean isLocal) {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return;

        Player playerToMove = isLocal ?
                (isServer ? gameState.getPlayer1() : gameState.getPlayer2()) :
                (isServer ? gameState.getPlayer2() : gameState.getPlayer1());

        playerToMove.move(direction);

        // Ограничиваем движение в пределах арены
        constrainPlayerToArena(playerToMove);

        // Проверяем столкновения
        checkCollisions();

        // Проверяем выпадение - ОДИН раз
        if (!roundCompletionInProgress && !roundEnded) {
            checkIfPlayerOut();
        }

        notifyStateUpdate();
    }

    private void constrainPlayerToArena(Player player) {
        double x = player.getX();
        double y = player.getY();
        double size = player.getCurrentSize();
        double maxX = GameConfig.ARENA_WIDTH;
        double maxY = GameConfig.ARENA_HEIGHT;

        // Допускаем выход за границы на размер игрока * 3 для плавного вылета
        x = Math.max(-size * 3, Math.min(maxX + size * 3, x));
        y = Math.max(-size * 3, Math.min(maxY + size * 3, y));

        player.setPosition(x, y);
    }

    private void checkCollisions() {
        Player p1 = gameState.getPlayer1();
        Player p2 = gameState.getPlayer2();

        if (p1.collidesWith(p2)) {
            // Вектор от p1 к p2
            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance == 0) {
                dx = 1;
                dy = 0;
                distance = 1;
            }

            double nx = dx / distance;
            double ny = dy / distance;

            double force1 = p1.getCurrentStrength() * 3.0;
            double force2 = p2.getCurrentStrength() * 3.0;

            // Отталкиваем игроков
            p1.setPosition(p1.getX() - nx * force2, p1.getY() - ny * force2);
            p2.setPosition(p2.getX() + nx * force1, p2.getY() + ny * force1);

            // Раздвигаем если перекрываются
            double overlap = (p1.getCurrentSize() + p2.getCurrentSize()) - distance;
            if (overlap > 0) {
                p1.setPosition(p1.getX() - nx * overlap * 0.5, p1.getY() - ny * overlap * 0.5);
                p2.setPosition(p2.getX() + nx * overlap * 0.5, p2.getY() + ny * overlap * 0.5);
            }
        }
    }

    private void checkIfPlayerOut() {
        if (roundCompletionInProgress || roundEnded) {
            return;
        }

        Player p1 = gameState.getPlayer1();
        Player p2 = gameState.getPlayer2();
        double arenaWidth = gameState.getArenaWidth();
        double arenaHeight = gameState.getArenaHeight();

        boolean p1Out = isPlayerOut(p1, arenaWidth, arenaHeight);
        boolean p2Out = isPlayerOut(p2, arenaWidth, arenaHeight);

        if (p1Out || p2Out) {
            roundCompletionInProgress = true;

            int winnerId;
            if (p1Out && p2Out) {
                winnerId = 0; // Ничья
                System.out.println("Результат: НИЧЬЯ! Оба вылетели");
            } else if (p1Out) {
                winnerId = 2; // Вылетел игрок 1
                System.out.println("Результат: ПОБЕДИЛ ИГРОК 2");
            } else {
                winnerId = 1; // Вылетел игрок 2
                System.out.println("Результат: ПОБЕДИЛ ИГРОК 1");
            }

            // Вызываем endRound в отдельном потоке с небольшой задержкой
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    Platform.runLater(() -> {
                        if (!roundEnded) {
                            endRound(winnerId);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private boolean isPlayerOut(Player player, double arenaWidth, double arenaHeight) {
        double x = player.getX();
        double y = player.getY();
        double size = player.getCurrentSize();

        // Все арены теперь круглые
        return isOutOfCircle(x, y, arenaWidth, arenaHeight, size);
    }

    private boolean isOutOfCircle(double x, double y, double arenaWidth, double arenaHeight, double playerSize) {
        double centerX = arenaWidth / 2;
        double centerY = arenaHeight / 2;
        double radius = Math.min(arenaWidth, arenaHeight) * 0.4;

        double dx = x - centerX;
        double dy = y - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Учитываем размер игрока - он вылетел если его центр за пределами круга
        return distance > radius;
    }

    private void endRound(int winnerId) {
        if (listener == null || roundEnded) {
            return;
        }

        roundEnded = true;
        gameState.setGameActive(false);

        System.out.println("=== РАУНД " + gameState.getRoundNumber() + " ЗАВЕРШЕН ===");
        System.out.println("Победитель раунда: " +
                (winnerId == 1 ? "Игрок 1" : winnerId == 2 ? "Игрок 2" : "Ничья"));

        // Сохраняем победителя раунда
        int currentRoundIndex = gameState.getRoundNumber() - 1;
        gameState.setRoundWinner(currentRoundIndex, winnerId);

        // Обновляем счет
        if (winnerId == 1) {
            gameState.setPlayer1Score(gameState.getPlayer1Score() + 1);
        } else if (winnerId == 2) {
            gameState.setPlayer2Score(gameState.getPlayer2Score() + 1);
        }

        System.out.println("Счет: " + gameState.getPlayer1Score() + " - " + gameState.getPlayer2Score());

        String roundResult;
        if (winnerId == 1) {
            roundResult = "Раунд " + gameState.getRoundNumber() + ": Победил Игрок 1";
        } else if (winnerId == 2) {
            roundResult = "Раунд " + gameState.getRoundNumber() + ": Победил Игрок 2";
        } else {
            roundResult = "Раунд " + gameState.getRoundNumber() + ": Ничья!";
        }

        listener.onGameEvent("ROUND_ENDED", roundResult);
        notifyStateUpdate();

        // Проверяем, все ли раунды сыграны ИЛИ есть победитель с 2 очками
        if (gameState.allRoundsPlayed() || gameState.getPlayer1Score() >= 2 || gameState.getPlayer2Score() >= 2) {
            // Матч завершен
            gameState.setMatchFinished(true);

            int matchWinner = gameState.getMatchWinner();
            String winnerMessage;

            if (matchWinner == 1) {
                winnerMessage = "Игрок 1 победил в матче со счетом " +
                        gameState.getPlayer1Score() + ":" + gameState.getPlayer2Score() + "!";
                System.out.println("🏆 ПОБЕДИТЕЛЬ МАТЧА: ИГРОК 1 🏆");
            } else if (matchWinner == 2) {
                winnerMessage = "Игрок 2 победил в матче со счетом " +
                        gameState.getPlayer1Score() + ":" + gameState.getPlayer2Score() + "!";
                System.out.println("🏆 ПОБЕДИТЕЛЬ МАТЧА: ИГРОК 2 🏆");
            } else {
                winnerMessage = "Ничья! Счет " +
                        gameState.getPlayer1Score() + ":" + gameState.getPlayer2Score();
                System.out.println("🤝 МАТЧ ЗАКОНЧИЛСЯ ВНИЧЬЮ 🤝");
            }

            listener.onGameEvent("MATCH_FINISHED", winnerMessage);

            // Через 3 секунды показываем итоги
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> showMatchResults());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            // Запускаем следующий раунд через 3 секунды
            System.out.println("Запуск следующего раунда через 3 секунды...");
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> startNewRound());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        roundCompletionInProgress = false;
    }

    private void startNewRound() {
        // Проверяем, не завершился ли матч
        if (gameState.isMatchFinished() || gameState.allRoundsPlayed()) {
            System.out.println("Матч завершен, новый раунд не запускается");

            // Если матч завершен, но не были показаны результаты - показываем
            if (gameState.isMatchFinished() && listener != null) {
                showMatchResults();
            }
            return;
        }

        // Увеличиваем номер раунда только если можем начать новый
        if (gameState.getRoundNumber() < GameConfig.TOTAL_ROUNDS) {
            gameState.incrementRoundNumber();
        } else {
            System.out.println("Все раунды сыграны, начинаем матч заново?");
            return;
        }

        roundTimer = GameConfig.ROUND_DURATION;
        gameState.setRoundTime(roundTimer);
        gameState.setGameActive(true);
        roundEnded = false;
        roundCompletionInProgress = false;

        // Устанавливаем арену для текущего раунда
        ArenaType arena = ArenaType.getByRoundNumber(gameState.getRoundNumber());
        gameState.setCurrentArena(arena);

        // Сбрасываем позиции - всегда в одинаковых позициях для круглой арены
        resetPlayerPositionsForArena();

        System.out.println("=== НАЧАЛСЯ РАУНД " + gameState.getRoundNumber() + " ===");
        System.out.println("Арена: " + arena.getName());
        System.out.println("Счет: " + gameState.getPlayer1Score() + " - " + gameState.getPlayer2Score());

        listener.onGameEvent("ROUND_STARTED",
                "Раунд " + gameState.getRoundNumber() + " из 3 - " + arena.getName());
        notifyStateUpdate();
    }

    private void resetPlayerPositionsForArena() {
        double arenaWidth = gameState.getArenaWidth();
        double arenaHeight = gameState.getArenaHeight();

        // Все арены круглые - одинаковые стартовые позиции
        double centerX = arenaWidth / 2;
        double centerY = arenaHeight / 2;
        double radius = Math.min(arenaWidth, arenaHeight) * 0.4;
        double offsetX = radius * 0.7;

        gameState.getPlayer1().resetForNewRound(centerX - offsetX, centerY);
        gameState.getPlayer2().resetForNewRound(centerX + offsetX, centerY);

        System.out.println("Позиции игроков сброшены:");
        System.out.println("Игрок 1: (" + (centerX - offsetX) + ", " + centerY + ")");
        System.out.println("Игрок 2: (" + (centerX + offsetX) + ", " + centerY + ")");
    }

    private void showMatchResults() {
        if (listener != null) {
            System.out.println("=== ПОКАЗЫВАЕМ РЕЗУЛЬТАТЫ МАТЧА ===");
            listener.onGameEvent("SHOW_RESULTS", "");
        }
    }

    public void update(double deltaTime) {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return;

        // Обновляем таймер
        roundTimer -= deltaTime;
        gameState.setRoundTime(Math.max(0, roundTimer));

        if (roundTimer <= 0 && !roundEnded) {
            // Время вышло - ничья
            System.out.println("ВРЕМЯ ВЫШЛО! Ничья в раунде " + gameState.getRoundNumber());
            roundCompletionInProgress = true;
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    Platform.runLater(() -> {
                        if (!roundEnded) {
                            endRound(0);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Обновляем состояния игроков (для способностей)
        gameState.getPlayer1().update(deltaTime);
        gameState.getPlayer2().update(deltaTime);

        notifyStateUpdate();
    }

    private void notifyStateUpdate() {
        if (listener != null) {
            listener.onGameStateUpdated(gameState);
        }
    }

    public boolean activatePowerUp() {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return false;

        Player localPlayer = isServer ? gameState.getPlayer1() : gameState.getPlayer2();
        boolean activated = localPlayer.activatePowerUp();

        if (activated) {
            System.out.println(localPlayer.getType().getName() + " использовал способность!");
            notifyStateUpdate();
        }

        return activated;
    }

    public void processOpponentPowerUp() {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return;

        Player opponent = isServer ? gameState.getPlayer2() : gameState.getPlayer1();
        opponent.activatePowerUp();
        System.out.println("Противник использовал способность!");
        notifyStateUpdate();
    }

    // Геттеры
    public GameState getGameState() {
        return gameState;
    }

    public boolean canProcessInput() {
        return gameState.isGameActive() && !roundEnded && !gameState.isMatchFinished();
    }

    public boolean canActivatePowerUp() {
        Player localPlayer = isServer ? gameState.getPlayer1() : gameState.getPlayer2();
        return gameState.isGameActive() && !roundEnded && !gameState.isMatchFinished() && localPlayer.isPowerUpAvailable();
    }

    public boolean isMatchFinished() {
        return gameState.isMatchFinished();
    }

    public void setGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }
}