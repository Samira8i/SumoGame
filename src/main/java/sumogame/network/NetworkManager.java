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

    private CharacterType myCharacter;
    private boolean opponentConnected = false;

    public NetworkManager(GameController controller, boolean isServer) {
        this.gameController = controller;
        this.isServer = isServer;
        //связуем и исходя из параметра создаю сервер/клиент
        if (isServer) {
            this.networkService = new GameServer(this);
        } else {
            String serverAddress = controller.getServerAddress();
            this.networkService = new GameClient(this, serverAddress);
        }
    }
    //todo: понять
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
                handleGameEvent(message.getData());
                break;

            case PLAYER_JOIN:
                handlePlayerJoin(message.getData(), message.getPlayerId());
                break;

            default:
                System.out.println("Необработанный тип сообщения: " + message.getType());
        }
    }

    private void handleGameEvent(String data) {
        String[] parts = data.split(":", 2);
        if (parts.length == 2) {
            gameController.onGameEvent(parts[0], parts[1]);
        } else {
            gameController.onGameEvent(data, "");
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

        if (gameController != null) {
            gameController.onGameEvent("PLAYER_DISCONNECTED", String.valueOf(playerId));
        }
    }

}