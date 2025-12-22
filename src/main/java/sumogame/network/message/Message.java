package sumogame.network.message;

import com.google.gson.Gson;

public class Message {

    public enum Type {
        PLAYER_JOIN,      // Подключение игрока
        PLAYER_MOVE,      // Движение игрока
        GAME_STATE,       // Состояние игры
        POWER_UP,         // Активация способности
        GAME_EVENT,       // Игровое событие
        PLAYER_READY,     // Игрок готов
        MATCH_RESULT      // Результат матча
    }

    private final Type type;
    private final String data;
    private final int playerId;
    private final long timestamp;

    public Type getType() { return type; }
    public String getData() { return data; }
    public int getPlayerId() { return playerId; }
    public long getTimestamp() { return timestamp; }

    public Message(Type type, String data, int playerId) {
        this.type = type;
        this.data = data;
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    public boolean isValid() {
        return type != null && data != null && timestamp > 0;
    }

    @Override
    public String toString() {
        return String.format("Message{type=%s, data='%s', playerId=%d}",
                type, data, playerId);
    }
}