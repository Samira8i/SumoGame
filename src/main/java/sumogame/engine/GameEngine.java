package sumogame.engine;

import javafx.application.Platform;
import sumogame.controller.GameController;
import sumogame.model.*;
import sumogame.model.Arena;

public class GameEngine {
    private GameState gameState;
    private GameController gameController;  // Прямая ссылка на контроллер
    private boolean isServer;
    private CharacterType localCharacter;
    private double roundTimer;
    private boolean roundEnded;
    private boolean roundCompletionInProgress = false;
    private boolean gameInitialized = false;

    public GameEngine(CharacterType localCharacter, boolean isServer, GameController controller) {
        this.localCharacter = localCharacter;
        this.isServer = isServer;
        this.gameController = controller;  // Сохраняем ссылку
        this.roundEnded = false;
        this.roundCompletionInProgress = false;
        initializeGameState();
    }

    private void initializeGameState() {
        Arena arena = new Arena(ArenaType.PINK_CIRCLE);

        CharacterType player1Type, player2Type;
        if (isServer) {
            player1Type = localCharacter;
            player2Type = CharacterType.PINK;
        } else {
            player1Type = CharacterType.PINK;
            player2Type = localCharacter;
        }

        Player player1 = new Player(1, player1Type, arena.getPlayer1StartX(), arena.getPlayer1StartY());
        Player player2 = new Player(2, player2Type, arena.getPlayer2StartX(), arena.getPlayer2StartY());

        this.gameState = new GameState();
        gameState.setPlayer1(player1);
        gameState.setPlayer2(player2);
        gameState.setRoundTime(GameConfig.ROUND_DURATION);

        this.roundTimer = GameConfig.ROUND_DURATION;
        gameState.setGameActive(false);
        roundEnded = false;

        System.out.println("=== ИГРА ПОДГОТОВЛЕНА К ЗАПУСКУ ===");
        System.out.println("Режим: " + (isServer ? "СЕРВЕР" : "КЛИЕНТ"));
        System.out.println("Игрок 1: " + player1Type.getName());
        System.out.println("Игрок 2: " + player2Type.getName());
        System.out.println("Арена: " + gameState.getCurrentArena().getType().getName());
    }

    public void startGame() {
        if (gameInitialized) return;

        gameState.setGameActive(true);
        gameInitialized = true;

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    System.out.println("✅ Игра началась!");
                    System.out.println("🚀 Раунд 1 из 3 - " + gameState.getCurrentArena().getType().getName());
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        System.out.println("=== ИГРА НАЧАЛАСЬ ===");
        System.out.println("Раунд: 1/3");
        System.out.println("Арена: " + gameState.getCurrentArena().getType().getName());
        System.out.println("Счет: 0 - 0");
    }

    public void updateOpponentCharacter(CharacterType opponentCharacter) {
        if (isServer) {
            gameState.getPlayer2().setType(opponentCharacter);
            System.out.println("Клиент выбрал: " + opponentCharacter.getName());
        } else {
            gameState.getPlayer1().setType(opponentCharacter);
            System.out.println("Сервер выбрал: " + opponentCharacter.getName());
        }
    }

    public void processPlayerInput(String directionStr, boolean isLocal) {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return;

        Direction direction;
        try {
            direction = Direction.valueOf(directionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Неверное направление: " + directionStr);
            return;
        }

        Player playerToMove = isLocal ?
                (isServer ? gameState.getPlayer1() : gameState.getPlayer2()) :
                (isServer ? gameState.getPlayer2() : gameState.getPlayer1());

        playerToMove.move(direction);
        constrainPlayerToArena(playerToMove);
        checkCollisions();

        if (!roundCompletionInProgress && !roundEnded) {
            checkIfPlayerOut();
        }
    }

    private void constrainPlayerToArena(Player player) {
        double x = player.getX();
        double y = player.getY();
        double size = player.getCurrentSize();
        double maxX = GameConfig.ARENA_WIDTH;
        double maxY = GameConfig.ARENA_HEIGHT;

        x = Math.max(-size * 3, Math.min(maxX + size * 3, x));
        y = Math.max(-size * 3, Math.min(maxY + size * 3, y));

        player.setPosition(x, y);
    }

    private void checkCollisions() {
        Player p1 = gameState.getPlayer1();
        Player p2 = gameState.getPlayer2();

        if (p1.collidesWith(p2)) {
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

            p1.setPosition(p1.getX() - nx * force2, p1.getY() - ny * force2);
            p2.setPosition(p2.getX() + nx * force1, p2.getY() + ny * force1);

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
        Arena arena = gameState.getCurrentArena();

        boolean p1Out = arena.isPlayerOut(p1);
        boolean p2Out = arena.isPlayerOut(p2);

        if (p1Out || p2Out) {
            roundCompletionInProgress = true;

            int winnerId;
            if (p1Out && p2Out) {
                winnerId = 0;
                System.out.println("Результат: НИЧЬЯ! Оба вылетели");
            } else if (p1Out) {
                winnerId = 2;
                System.out.println("Результат: ПОБЕДИЛ ИГРОК 2");
            } else {
                winnerId = 1;
                System.out.println("Результат: ПОБЕДИЛ ИГРОК 1");
            }

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

    private void endRound(int winnerId) {
        if (roundEnded) {
            return;
        }

        roundEnded = true;
        gameState.setGameActive(false);

        System.out.println("=== РАУНД " + gameState.getRoundNumber() + " ЗАВЕРШЕН ===");
        System.out.println("Победитель раунда: " +
                (winnerId == 1 ? "Игрок 1" : winnerId == 2 ? "Игрок 2" : "Ничья"));

        int currentRoundIndex = gameState.getRoundNumber() - 1;
        gameState.setRoundWinner(currentRoundIndex, winnerId);

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

        System.out.println("🏁 " + roundResult);

        // Проверяем, завершен ли матч
        boolean matchFinished = false;

        // 1. Проверяем, набрал ли кто-то 2 очка
        if (gameState.getPlayer1Score() >= 2) {
            gameState.setMatchWinner(1);
            matchFinished = true;
            System.out.println("🏆 ПОБЕДИТЕЛЬ МАТЧА: ИГРОК 1 🏆");
        } else if (gameState.getPlayer2Score() >= 2) {
            gameState.setMatchWinner(2);
            matchFinished = true;
            System.out.println("🏆 ПОБЕДИТЕЛЬ МАТЧА: ИГРОК 2 🏆");
        }
        // 2. Проверяем, сыграны ли все раунды
        else if (gameState.allRoundsPlayed()) {
            // Определяем победителя по очкам
            if (gameState.getPlayer1Score() > gameState.getPlayer2Score()) {
                gameState.setMatchWinner(1);
                System.out.println("🏆 ПОБЕДИТЕЛЬ МАТЧА: ИГРОК 1 🏆");
            } else if (gameState.getPlayer2Score() > gameState.getPlayer1Score()) {
                gameState.setMatchWinner(2);
                System.out.println("🏆 ПОБЕДИТЕЛЬ МАТЧА: ИГРОК 2 🏆");
            } else {
                gameState.setMatchWinner(0);
                System.out.println("🤝 МАТЧ ЗАКОНЧИЛСЯ ВНИЧЬЮ 🤝");
            }
            matchFinished = true;
        }

        if (matchFinished) {
            gameState.setMatchFinished(true);

            String winnerMessage;
            int matchWinner = gameState.getMatchWinner();

            if (matchWinner == 1) {
                winnerMessage = "Игрок 1 победил в матче со счетом " +
                        gameState.getPlayer1Score() + ":" + gameState.getPlayer2Score() + "!";
            } else if (matchWinner == 2) {
                winnerMessage = "Игрок 2 победил в матче со счетом " +
                        gameState.getPlayer1Score() + ":" + gameState.getPlayer2Score() + "!";
            } else {
                winnerMessage = "Ничья! Счет " +
                        gameState.getPlayer1Score() + ":" + gameState.getPlayer2Score();
            }

            System.out.println("🎮 " + winnerMessage);

            // Задержка перед показом результатов
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> showMatchResults());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
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
        if (gameState.isMatchFinished() || gameState.allRoundsPlayed()) {
            System.out.println("Матч завершен, новый раунд не запускается");
            if (gameState.isMatchFinished()) {
                showMatchResults();
            }
            return;
        }

        if (gameState.getRoundNumber() < GameConfig.TOTAL_ROUNDS) {
            gameState.incrementRoundNumber();
        } else {
            System.out.println("Все раунды сыграны");
            gameState.setMatchFinished(true);
            showMatchResults();
            return;
        }

        roundTimer = GameConfig.ROUND_DURATION;
        gameState.setRoundTime(roundTimer);
        gameState.setGameActive(true);
        roundEnded = false;
        roundCompletionInProgress = false;

        ArenaType[] arenaTypes = ArenaType.values();
        int arenaIndex = (gameState.getRoundNumber() - 1) % arenaTypes.length;
        gameState.setCurrentArena(arenaTypes[arenaIndex]);

        resetPlayerPositionsForArena();

        System.out.println("=== НАЧАЛСЯ РАУНД " + gameState.getRoundNumber() + " ===");
        System.out.println("Арена: " + gameState.getCurrentArena().getType().getName());
        System.out.println("Счет: " + gameState.getPlayer1Score() + " - " + gameState.getPlayer2Score());

        System.out.println("🚀 Раунд " + gameState.getRoundNumber() + " из 3 - " +
                gameState.getCurrentArena().getType().getName());
    }

    private void resetPlayerPositionsForArena() {
        Arena arena = gameState.getCurrentArena();
        gameState.getPlayer1().resetForNewRound(arena.getPlayer1StartX(), arena.getPlayer1StartY());
        gameState.getPlayer2().resetForNewRound(arena.getPlayer2StartX(), arena.getPlayer2StartY());

        System.out.println("Позиции игроков сброшены:");
        System.out.println("Игрок 1: (" + arena.getPlayer1StartX() + ", " + arena.getPlayer1StartY() + ")");
        System.out.println("Игрок 2: (" + arena.getPlayer2StartX() + ", " + arena.getPlayer2StartY() + ")");
    }

    private void showMatchResults() {
        System.out.println("=== ПОКАЗЫВАЕМ РЕЗУЛЬТАТЫ МАТЧА ===");
        if (gameController != null) {
            gameController.showMatchResults();  // Прямой вызов!
        }
    }

    public void update(double deltaTime) {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return;

        roundTimer -= deltaTime;
        gameState.setRoundTime(Math.max(0, roundTimer));

        if (roundTimer <= 0 && !roundEnded) {
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

        gameState.getPlayer1().update(deltaTime);
        gameState.getPlayer2().update(deltaTime);
    }

    public boolean activatePowerUp() {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return false;

        Player localPlayer = isServer ? gameState.getPlayer1() : gameState.getPlayer2();
        boolean activated = localPlayer.activatePowerUp();

        if (activated) {
            System.out.println(localPlayer.getType().getName() + " использовал способность!");
        }

        return activated;
    }

    public void processOpponentPowerUp() {
        if (!gameState.isGameActive() || roundEnded || gameState.isMatchFinished()) return;

        Player opponent = isServer ? gameState.getPlayer2() : gameState.getPlayer1();
        opponent.activatePowerUp();
        System.out.println("Противник использовал способность!");
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
}