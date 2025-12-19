package sumogame.model;

import sumogame.util.DebugLogger;

public class GameState {
    private Player player1;
    private Player player2;
    private int player1Score = 0;
    private int player2Score = 0;
    private int roundNumber = 1;
    private double roundTime;
    private boolean gameActive = false;
    private boolean matchFinished = false;
    private ArenaType currentArena;
    private int[] roundWinners;
    private double arenaWidth;
    private double arenaHeight;

    public GameState() {
        roundWinners = new int[GameConfig.TOTAL_ROUNDS];
    }

    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player) { this.player1 = player; }

    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player) { this.player2 = player; }

    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int score) {
        this.player1Score = score;
        DebugLogger.log("Счет игрока 1: " + score);
    }

    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int score) {
        this.player2Score = score;
        DebugLogger.log("Счет игрока 2: " + score);
    }

    public int getRoundNumber() { return roundNumber; }

    public void incrementRoundNumber() {
        if (roundNumber < GameConfig.TOTAL_ROUNDS) {
            roundNumber++;
            DebugLogger.log("НОВЫЙ РАУНД: " + roundNumber);
        } else {
            DebugLogger.log("Не могу увеличить номер раунда, матч завершен");
        }
    }

    public double getRoundTime() { return roundTime; }
    public void setRoundTime(double time) { this.roundTime = time; }

    public boolean isGameActive() { return gameActive; }

    public void setGameActive(boolean active) {
        this.gameActive = active;
        DebugLogger.log("Игра активна: " + active);
    }

    public boolean isMatchFinished() { return matchFinished; }

    public void setMatchFinished(boolean finished) {
        this.matchFinished = finished;
        DebugLogger.log("Матч завершен: " + finished);
    }

    public double getArenaWidth() { return arenaWidth; }
    public void setArenaWidth(double width) { this.arenaWidth = width; }

    public double getArenaHeight() { return arenaHeight; }
    public void setArenaHeight(double height) { this.arenaHeight = height; }

    public ArenaType getCurrentArena() { return currentArena; }

    public void setCurrentArena(ArenaType arena) {
        this.currentArena = arena;
        DebugLogger.log("Установлена арена: " + arena.getName());
    }

    public int[] getRoundWinners() { return roundWinners; }

    public void setRoundWinner(int roundIndex, int winnerId) {
        if (roundIndex >= 0 && roundIndex < roundWinners.length) {
            roundWinners[roundIndex] = winnerId;
            String winnerName = winnerId == 1 ? "Игрок 1" :
                    winnerId == 2 ? "Игрок 2" : "Ничья";
            DebugLogger.log("Раунд " + (roundIndex + 1) + ": победитель = " + winnerName);
        }
    }

    public boolean allRoundsPlayed() {
        return roundNumber > GameConfig.TOTAL_ROUNDS;
    }

    public int getMatchWinner() {
        if (player1Score > player2Score) return 1;
        if (player2Score > player1Score) return 2;
        return 0;
    }
}