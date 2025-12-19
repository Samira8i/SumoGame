package sumogame.model;

import sumogame.util.DebugLogger;

public class Message {
    public enum Type {
        PLAYER_JOIN, PLAYER_MOVE, GAME_STATE, POWER_UP, GAME_EVENT
    }

    private Type type;
    private String data;
    private int playerId;
    private long timestamp;

    public Message() {}

    public Message(Type type, String data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        logMessage();
    }

    public Message(Type type, String data, int playerId) {
        this.type = type;
        this.data = data;
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
        logMessage();
    }

    private void logMessage() {
        DebugLogger.log("Создано сообщение: " + type + ", данные: '" + data + "', игрок: " + playerId);
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Message{type=" + type + ", data='" + data + "', playerId=" + playerId + "}";
    }
}