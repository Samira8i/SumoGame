package sumogame.network;

import sumogame.network.service.NetworkService;
import sumogame.network.service.GameServer;
import sumogame.network.service.GameClient;
import sumogame.network.message.Message;
import sumogame.network.message.MessageHandler;
import sumogame.controller.GameController;
import sumogame.model.CharacterType;

public class NetworkManager implements MessageHandler {
    private final NetworkService networkService;
    private final GameController gameController;
    private final boolean isServer;
    private final int port;

    private CharacterType myCharacter;
    private boolean opponentConnected = false;

    public NetworkManager(GameController controller, boolean isServer) {
        this.gameController = controller;
        this.isServer = isServer;
        this.port = controller.getPort();

        // создаем сервер/клиент в зависимости от параметра
        if (isServer) {
            this.networkService = new GameServer(this, port);
        } else {
            String serverAddress = controller.getServerAddress();
            this.networkService = new GameClient(this, serverAddress, port);
        }
    }

    // Альтернативный конструктор с явным указанием порта
    public NetworkManager(GameController controller, boolean isServer, int port) {
        this.gameController = controller;
        this.isServer = isServer;
        this.port = port;

        if (isServer) {
            this.networkService = new GameServer(this, port);
        } else {
            String serverAddress = controller.getServerAddress();
            this.networkService = new GameClient(this, serverAddress, port);
        }
    }

    public void setMyCharacter(CharacterType character) {
        this.myCharacter = character;
        if (isServer && networkService instanceof GameServer) {
            ((GameServer) networkService).setServerCharacter(character);
        }
    }

    public void startNetwork() {
        if (isServer) {
            networkService.startServer();
        } else {
            throw new UnsupportedOperationException("Клиент не может запустить сервер");
        }
    }

    public void connect(String address) {
        if (isServer) {
            return; // Сервер не подключается к другим серверам
        }
        networkService.connect(address);
    }

    public void sendPlayerMove(String direction) {
        if (!networkService.isConnected()) return;

        Message message = new Message(
                Message.Type.PLAYER_MOVE,
                direction,
                networkService.getPlayerId()
        );
        networkService.sendMessage(message);
    }

    public void sendPowerUp() {
        if (!networkService.isConnected()) return;

        Message message = new Message(
                Message.Type.POWER_UP,
                "ACTIVATE",
                networkService.getPlayerId()
        );
        networkService.sendMessage(message);
    }

    public void sendPlayerJoin(String characterName) {
        if (!networkService.isConnected()) return;

        Message message = new Message(
                Message.Type.PLAYER_JOIN,
                characterName,
                networkService.getPlayerId()
        );
        networkService.sendMessage(message);
        System.out.println("Отправлен персонаж: " + characterName);
    }

    public void disconnect() {
        networkService.disconnect();
    }

    // Реализация MessageHandler
    @Override
    public void handleMessage(Message message) {
        if (gameController == null) return;

        switch (message.getType()) {
            case PLAYER_MOVE:
                gameController.handleNetworkInput(message.getData());
                break;

            case POWER_UP:
                gameController.handleNetworkPowerUp();
                break;

            case GAME_EVENT:
                System.out.println("Получено GAME_EVENT сообщение: " + message.getData());
                break;

            case PLAYER_JOIN:
                handlePlayerJoin(message.getData(), message.getPlayerId());
                break;

            default:
                System.out.println("Необработанный тип сообщения: " + message.getType());
        }
    }

    private void handlePlayerJoin(String characterName, int playerId) {
        try {
            CharacterType character = CharacterType.valueOf(characterName);
            System.out.println("Получен персонаж игрока " + playerId + ": " + character.getName());
            gameController.updateOpponentCharacter(character);
        } catch (IllegalArgumentException e) {
            System.err.println("Неизвестный тип персонажа: " + characterName);
        }
    }

    @Override
    public void onClientConnected(int playerId) {
        System.out.println("Противник подключился: Player " + playerId);
        opponentConnected = true;

        if (gameController != null) {
            gameController.onOpponentConnected();
        }
    }

    @Override
    public void onClientDisconnected(int playerId) {
        System.out.println("Противник отключился: Player " + playerId);
        opponentConnected = false;

        System.out.println("Игрок отключился. Можно добавить метод в GameController при необходимости.");
    }

    // Геттеры для информации о подключении
    public int getPort() {
        return port;
    }

    public String getConnectionInfo() {
        if (isServer) {
            return "Сервер на порту: " + port;
        } else {
            String address = (networkService instanceof GameClient) ?
                    ((GameClient) networkService).getServerAddress() : "неизвестно";
            return "Клиент: " + address + ":" + port;
        }
    }
}