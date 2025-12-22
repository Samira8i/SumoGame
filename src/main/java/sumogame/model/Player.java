package sumogame.model;

public class Player {
    private final int playerId;
    private CharacterType type;
    private static final double BASE_STRENGTH = GameConfig.PLAYER_BASE_STRENGTH;
    private static final double BASE_SPEED = GameConfig.PLAYER_BASE_SPEED;
    public static final double BASE_SIZE = GameConfig.PLAYER_BASE_SIZE;

    private double currentStrength;
    private double currentSpeed;
    private double currentSize;
    private double x, y;
    private boolean powerUpAvailable;
    private boolean powerUpActive;
    private double powerUpTimer;
    private double originalStrength;
    private double originalSpeed;
    private double originalSize;

    public Player(int playerId, CharacterType characterType, double startX, double startY) {
        this.playerId = playerId;
        this.type = characterType;
        resetParameters();
        this.x = startX;
        this.y = startY;
        this.powerUpAvailable = true;
        this.powerUpActive = false;
        this.powerUpTimer = 0;
        System.out.println("Создан игрок " + playerId + ": " + characterType.getName() +
                " в позиции (" + startX + ", " + startY + ")");
    }

    public boolean activatePowerUp() {
        if (!powerUpAvailable || powerUpActive) {
            System.out.println("Игрок " + playerId + ": способность недоступна");
            return false;
        }

        powerUpAvailable = false;
        powerUpActive = true;
        powerUpTimer = type.getAbilityDuration();
        originalStrength = currentStrength;
        originalSpeed = currentSpeed;
        originalSize = currentSize;

        applyAbilityEffect();

        System.out.println("Игрок " + playerId + " активировал " + type.getAbilityName());
        return true;
    }

    private void applyAbilityEffect() {
        currentStrength = BASE_STRENGTH * type.getStrengthMultiplier();
        currentSpeed = BASE_SPEED * type.getSpeedMultiplier();
        currentSize = BASE_SIZE * type.getSizeMultiplier();
    }

    public void update(double deltaTime) {
        if (powerUpActive) {
            powerUpTimer -= deltaTime;
            if (powerUpTimer <= 0) {
                resetPowerUp();
            }
        }
    }

    private void resetPowerUp() {
        powerUpActive = false;
        currentStrength = originalStrength;
        currentSpeed = originalSpeed;
        currentSize = originalSize;
        System.out.println("Игрок " + playerId + ": способность закончилась");
    }

    public void resetForNewRound(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        resetParameters();
        this.powerUpAvailable = true;
        this.powerUpActive = false;
        this.powerUpTimer = 0;
        System.out.println("Игрок " + playerId + " сброшен в позицию (" + startX + ", " + startY + ")");
    }

    private void resetParameters() {
        this.currentStrength = BASE_STRENGTH;
        this.currentSpeed = BASE_SPEED;
        this.currentSize = BASE_SIZE;
        this.originalStrength = BASE_STRENGTH;
        this.originalSpeed = BASE_SPEED;
        this.originalSize = BASE_SIZE;
    }

    public void move(Direction direction) {
        if (direction == null) {
            System.out.println("Игрок " + playerId + ": попытка движения в null направлении");
            return;
        }

        double[] newPosition = direction.calculateNewPosition(x, y, currentSpeed);
        this.x = newPosition[0];
        this.y = newPosition[1];
    }

    public boolean collidesWith(Player other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = currentSize + other.currentSize;
        boolean collides = distance < minDistance;
        if (collides) {
            System.out.println("СТОЛКНОВЕНИЕ: Игрок " + playerId + " с Игроком " + other.playerId);
        }
        return collides;
    }

    public int getPlayerId() { return playerId; }
    public CharacterType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getCurrentSize() { return currentSize; }
    public double getCurrentSpeed() { return currentSpeed; }
    public double getCurrentStrength() { return currentStrength; }
    public boolean isPowerUpAvailable() { return powerUpAvailable; }
    public boolean isPowerUpActive() { return powerUpActive; }
    public double getPowerUpRemainingTime() { return Math.max(0, powerUpTimer); }
    public String getColorHex() { return type.getColorHex(); }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setType(CharacterType type) {
        this.type = type;
        System.out.println("Игрок " + playerId + " теперь: " + type.getName());
    }
}