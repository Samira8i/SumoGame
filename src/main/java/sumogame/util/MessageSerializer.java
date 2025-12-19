package sumogame.util;

import com.google.gson.Gson;
import sumogame.model.Message;

public class MessageSerializer {
    private static final Gson gson = new Gson();

    private MessageSerializer() {}

    public static String toJson(Message message) {
        return gson.toJson(message);
    }

    /**
     * Десериализация JSON в сообщение
     */
    public static Message fromJson(String json) {
        return gson.fromJson(json, Message.class);
    }

    /**
     * Валидация JSON перед десериализацией
     */
    public static boolean isValidMessage(String json) {
        try {
            Message message = fromJson(json);
            return message.getType() != null && message.getData() != null;
        } catch (Exception e) {
            return false;
        }
    }
}