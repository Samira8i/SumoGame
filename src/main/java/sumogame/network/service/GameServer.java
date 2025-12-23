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
    private int playerId = 1;
    private CharacterType serverCharacter;
    private int port;

    public GameServer(MessageHandler messageHandler, int port) {
        this.messageHandler = messageHandler;
        this.port = port;
    }

    public void setServerCharacter(CharacterType character) {
        this.serverCharacter = character;
        System.out.println("Сервер установил персонажа: " + character.getName());
    }

    @Override
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Сервер запущен на порту " + port);

            new Thread(this).start();

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера на порту " + port + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Ожидание подключения клиента на порту " + port + "...");
            clientSocket = serverSocket.accept();
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

            System.out.println("Клиент подключен на порту " + port + "!");

            if (messageHandler != null) {
                messageHandler.onClientConnected(2);
            }

            if (serverCharacter != null) {
                Message serverCharacterMessage = new Message(
                        Message.Type.PLAYER_JOIN,
                        serverCharacter.name(),
                        playerId
                );
                sendMessage(serverCharacterMessage);
                System.out.println("Отправлен персонаж сервера клиенту: " + serverCharacter.name());
            }

            listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();

        } catch (IOException e) {
            System.err.println("Ошибка подключения на порту " + port + ": " + e.getMessage());
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
            System.out.println("Соединение с клиентом разорвано на порту " + port);
        } finally {
            stop();
            if (messageHandler != null) {
                messageHandler.onClientDisconnected(2);
            }
        }
    }

    @Override
    public boolean connect(String address) {
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

    public int getPort() {
        return port;
    }
}