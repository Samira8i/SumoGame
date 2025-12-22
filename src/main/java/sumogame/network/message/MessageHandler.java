package sumogame.network.message;
/**
 * Обработчик сообщений
 */
public interface MessageHandler {
    void handleMessage(Message message);
    default void onClientConnected(int playerId) {}
    default void onClientDisconnected(int playerId) {}
}