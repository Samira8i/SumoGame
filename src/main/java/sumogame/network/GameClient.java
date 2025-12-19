package sumogame.network;

import sumogame.model.Message;
import sumogame.util.MessageSerializer;

import java.io.*;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private boolean connected;

    // Интерфейс для колбэков
    public interface ClientListener {
        void onMessageReceived(Message message);
        void onConnected(int playerId);
        void onDisconnected();
    }

    private ClientListener listener;

    public GameClient(ClientListener listener) {
        this.listener = listener;
    }

    public boolean connect(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            // Запускаем поток прослушивания
            listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();

            if (listener != null) {
                listener.onConnected(2); // Клиент всегда имеет ID 2
            }

            return true;

        } catch (IOException e) {
            System.err.println("Не удалось подключиться: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String messageJson;
            while (connected && (messageJson = in.readLine()) != null) {
                Message message = MessageSerializer.fromJson(messageJson);
                if (listener != null) {
                    listener.onMessageReceived(message);
                }
            }
        } catch (IOException e) {
            System.out.println("Соединение с сервером разорвано");
        } finally {
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }

    public void sendMessage(Message message) {
        if (connected && out != null) {
            String json = MessageSerializer.toJson(message);
            out.println(json);
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Игнорируем
        }
    }

    public boolean isConnected() {
        return connected;
    }
}