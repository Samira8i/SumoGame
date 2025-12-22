package sumogame.network.service;

import sumogame.model.CharacterType;
import sumogame.network.message.Message;
import sumogame.network.message.MessageHandler;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer implements NetworkService, Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private boolean running;
    private final MessageHandler messageHandler;
    private int playerId = 1; // Сервер всегда Player 1

    // Храним персонаж сервера
    private CharacterType serverCharacter;

    public GameServer(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setServerCharacter(CharacterType character) {
        this.serverCharacter = character;
        System.out.println("Сервер установил персонажа: " + character.getName());
    }

    @Override
    public void startServer() {
        try {
            serverSocket = new ServerSocket(8080); //todo:добавить возможность ввести порт
            running = true;
            System.out.println("Сервер запущен на порту 8080");

            // Запускаем в отдельном потоке для неблокирующего ожидания
            new Thread(this).start();

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Ожидание подключения клиента...");
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Клиент подключен!");

            // Уведомляем обработчик
            if (messageHandler != null) {
                messageHandler.onClientConnected(2); // Клиент всегда Player 2
            }

            // отправляю пенрсонаж сервера клиенту
            if (serverCharacter != null) {
                Message serverCharacterMessage = new Message(
                        Message.Type.PLAYER_JOIN,
                        serverCharacter.name(),
                        playerId
                );
                sendMessage(serverCharacterMessage);
                System.out.println("Отправлен персонаж сервера клиенту: " + serverCharacter.name());
            }

            // запускаю поток для чтения сообщений
            listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();

        } catch (IOException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
            stop();
        }
    }

    private void listenForMessages() {
        try {
            String messageJson;
            while (running && (messageJson = in.readLine()) != null) {
                System.out.println("Сервер получил сообщение: " + messageJson);
                Message message = Message.fromJson(messageJson);
                if (message.isValid() && messageHandler != null) {
                    messageHandler.handleMessage(message);
                }
            }
        } catch (IOException e) {
            System.out.println("Соединение с клиентом разорвано");
        } finally {
            stop();
            if (messageHandler != null) {
                messageHandler.onClientDisconnected(2);
            }
        }
    }

    //todo: зачем метод?
    @Override
    public boolean connect(String address) {
        // Сервер не подключается к другим серверам
        return false;
    }

    @Override
    public void sendMessage(Message message) {
        if (running && out != null && clientSocket != null && !clientSocket.isClosed()) {
            out.println(message.toJson());
            System.out.println("Сервер отправил сообщение: " + message.toJson());
        }
    }

    @Override
    public void disconnect() {
        stop();
    }

    @Override
    public boolean isConnected() {
        return running && clientSocket != null && !clientSocket.isClosed();
    }

    @Override
    public int getPlayerId() {
        return playerId;
    }

    private void stop() {
        running = false;
        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            // Игнорируем ошибки закрытия
        }
    }
}