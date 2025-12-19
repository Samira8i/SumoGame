package sumogame.network;

import sumogame.model.CharacterType;
import sumogame.model.Message;
import sumogame.util.MessageSerializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread clientThread;
    private boolean running;

    // Интерфейс для колбэков
    public interface ServerListener {
        void onClientMessage(Message message);
        void onClientConnected(int playerId);
        void onClientDisconnected(int playerId);
    }

    private ServerListener listener;

    public GameServer(ServerListener listener) {
        this.listener = listener;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(8080);
            running = true;

            System.out.println("Сервер запущен, ожидаем подключения...");

            // Ждем одно подключение
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Клиент подключен!");

            // Запускаем поток для чтения сообщений
            clientThread = new Thread(this::listenForMessages);
            clientThread.start();

            if (listener != null) {
                listener.onClientConnected(2); // Клиент всегда имеет ID 2
            }

        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                Message msg = MessageSerializer.fromJson(message);
                if (listener != null) {
                    listener.onClientMessage(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("Клиент отключился");
        } finally {
            if (listener != null) {
                listener.onClientDisconnected(2);
            }
        }
    }

    public void sendToClient(Message message) {
        if (out != null) {
            String json = MessageSerializer.toJson(message);
            out.println(json);
        }
    }

    public void stop() {
        running = false;
        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            // Игнорируем
        }
    }
}