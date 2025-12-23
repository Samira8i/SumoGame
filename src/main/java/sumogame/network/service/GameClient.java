package sumogame.network.service;

import sumogame.network.message.Message;
import sumogame.network.message.MessageHandler;

import java.io.*;
import java.net.Socket;
import java.net.ConnectException;

public class GameClient implements NetworkService, Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean connected;
    private final MessageHandler messageHandler;
    private final String serverAddress;
    private final int serverPort;
    private static final int CONNECT_TIMEOUT = 5000;
    private final int playerId = 2;

    public GameClient(MessageHandler messageHandler, String serverAddress, int serverPort) {
        this.messageHandler = messageHandler;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public boolean connect(String address) {
        try {
            System.out.println("Попытка подключения к " + serverAddress + ":" + serverPort);
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(serverAddress, serverPort), CONNECT_TIMEOUT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            connected = true;

            System.out.println("Успешно подключено к серверу: " + serverAddress + ":" + serverPort);

            listenerThread = new Thread(this, "Client-Listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            return true;

        } catch (ConnectException e) {
            System.err.println("Не удалось подключиться к серверу " + serverAddress + ":" + serverPort + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("Ошибка подключения к " + serverAddress + ":" + serverPort + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public void run() {
        listenForMessages();
    }

    private void listenForMessages() {
        try {
            String messageJson;
            while (connected && (messageJson = in.readLine()) != null) {
                Message message = Message.fromJson(messageJson);
                if (message != null && message.isValid() && messageHandler != null) {
                    messageHandler.handleMessage(message);
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("Соединение с сервером " + serverAddress + ":" + serverPort + " разорвано: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    @Override
    public void startServer() {
        throw new UnsupportedOperationException("Клиент не может запустить сервер");
    }

    @Override
    public synchronized void sendMessage(Message message) {
        if (connected && out != null) {
            out.println(message.toJson());
        }
    }

    @Override
    public synchronized void disconnect() {
        if (!connected) return;

        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Игнорирую ошибки при закрытии
        } finally {
            if (messageHandler != null) {
                messageHandler.onClientDisconnected(playerId);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    @Override
    public int getPlayerId() {
        return playerId;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}