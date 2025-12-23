package sumogame.network.message;

import com.google.gson.Gson;

public class Message {

    public enum Type {
        PLAYER_JOIN,      // Подключение игрока + выбор персонажа
        PLAYER_MOVE,      // Движение игрока
        POWER_UP,         // Активация способности
        ROUND_RESULT      // Результат раунда (сервер → клиент) такой немноого странный ти
        //прежде всего для сверки, чтобы не было рассинхрона
    }

    private final Type type;
    private final String data;
    private final int playerId;

    public Type getType() { return type; }
    public String getData() { return data; }
    public int getPlayerId() { return playerId; }

    public Message(Type type, String data, int playerId) {
        this.type = type;
        this.data = data;
        this.playerId = playerId;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    public boolean isValid() {
        return type != null && data != null && (playerId == 1 || playerId == 2);
    }

    @Override
    public String toString() {
        return String.format("Message{type=%s, playerId=%d, data='%s'}",
                type, playerId, data);
    }
}