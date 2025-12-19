package sumogame.engine;

import sumogame.model.GameState;

public interface GameEventListener {
    void onGameStateUpdated(GameState state);  // Для отрисовки
    void onGameEvent(String eventType, String data);  // Для событий
}