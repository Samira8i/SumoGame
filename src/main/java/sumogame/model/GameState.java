package sumogame.model;

public class GameState {
    private Player player1;
    private Player player2;
    private int player1Score = 0;
    private int player2Score = 0;
    private int roundNumber = 1;
    private double roundTime;
    private boolean gameActive = false;
    private boolean matchFinished = false;
    private Arena currentArena;
    private int[] roundWinners;
    private int matchWinner = -1;

    public GameState() {
        roundWinners = new int[GameConfig.TOTAL_ROUNDS];
        this.currentArena = new Arena(ArenaType.PINK_CIRCLE);
    }

    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player) { this.player1 = player; }

    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player) { this.player2 = player; }

    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int score) {
        this.player1Score = score;
        System.out.println("Счет игрока 1: " + score);
    }

    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int score) {
        this.player2Score = score;
        System.out.println("Счет игрока 2: " + score);
    }

    public int getRoundNumber() { return roundNumber; }

    public void incrementRoundNumber() {
        if (roundNumber < GameConfig.TOTAL_ROUNDS) {
            roundNumber++;
            System.out.println("НОВЫЙ РАУНД: " + roundNumber);
        } else {
            System.out.println("Не могу увеличить номер раунда, матч завершен");
        }
    }

    public double getRoundTime() { return roundTime; }
    public void setRoundTime(double time) { this.roundTime = time; }

    public boolean isGameActive() { return gameActive; }

    public void setGameActive(boolean active) {
        this.gameActive = active;
        System.out.println("Игра активна: " + active);
    }

    public boolean isMatchFinished() { return matchFinished; }

    public void setMatchFinished(boolean finished) {
        this.matchFinished = finished;
        System.out.println("Матч завершен: " + finished);
    }
    public Arena getCurrentArena() { return currentArena; }

    public void setCurrentArena(ArenaType arenaType) {
        this.currentArena = new Arena(arenaType); // Создаем новый объект Arena
        System.out.println("Установлена арена: " + arenaType.getName());
    }

    public int[] getRoundWinners() { return roundWinners; }

    public void setRoundWinner(int roundIndex, int winnerId) {
        if (roundIndex >= 0 && roundIndex < roundWinners.length) {
            roundWinners[roundIndex] = winnerId;
            String winnerName = winnerId == 1 ? "Игрок 1" :
                    winnerId == 2 ? "Игрок 2" : "Ничья";
            System.out.println("Раунд " + (roundIndex + 1) + ": победитель = " + winnerName);
        }
    }

    public boolean allRoundsPlayed() {
        return roundNumber >= GameConfig.TOTAL_ROUNDS;
    }

    public int getMatchWinner() {
        // Если matchWinner уже установлен, используем его
        if (matchWinner != -1) {
            return matchWinner;
        }
        // Иначе вычисляем по очкам
        if (player1Score > player2Score) return 1;
        if (player2Score > player1Score) return 2;
        return 0;
    }
    // В классе GameState добавьте:
    public void setMatchWinner(int matchWinner) {
        this.matchWinner = matchWinner;
    }
}