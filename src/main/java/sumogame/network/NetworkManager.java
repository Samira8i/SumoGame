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

    public void setMyCharacter(CharacterType character) {
        this.myCharacter = character;
        if (isServer && networkService instanceof GameServer) {
            ((GameServer) networkService).setServerCharacter(character);
        } // клиент отправляет персонаж через сообщение PLAYER_JOIN
    }

    public void startNetwork() {
        if (isServer) {
            networkService.startServer();
            System.out.println("Сервер запущен на порту " + port);
        } else {
            throw new UnsupportedOperationException("Клиент не может запустить сервер");
        }
    }

    public void connect(String address) {
        if (isServer) {
            return; // Сервер не подключается к другим серверам
        }

        boolean connected = networkService.connect(address);
        if (connected) {
            System.out.println("Клиент подключен к " + address + ":" + port);
        } else {
            System.out.println("Не удалось подключиться к " + address + ":" + port);
        }
    }

    // Отправка движения игрока
    public void sendPlayerMove(String direction) {
        if (!networkService.isConnected()) {
            System.out.println("Не подключен к сети, движение не отправлено");
            return;
        }

        Message message = new Message(
                Message.Type.PLAYER_MOVE,
                direction,
                networkService.getPlayerId()
        );
        networkService.sendMessage(message);
        System.out.println("Отправлено движение: " + direction);
    }

    // Отправка активации способности
    public void sendPowerUp() {
        if (!networkService.isConnected()) return;

        Message message = new Message(
                Message.Type.POWER_UP,
                "ACTIVATE",
                networkService.getPlayerId()
        );
        networkService.sendMessage(message);
        System.out.println("Отправлена активация способности");
    }

    // Отправка выбора персонажа
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

    // Отправка результата раунда
    public void sendRoundResult(int winnerId) {
        if (!isServer) {
            System.out.println("Клиент не может отправлять результаты раунда");
            return;
        }

        if (!networkService.isConnected()) return;

        Message message = new Message(
                Message.Type.ROUND_RESULT,
                String.valueOf(winnerId),
                networkService.getPlayerId()
        );
        networkService.sendMessage(message);
        System.out.println("Сервер отправил результат раунда: победитель " + winnerId);
    }

    public void disconnect() {
        networkService.disconnect();
    }

    // реализация MessageHandler
    @Override
    public void handleMessage(Message message) {
        if (gameController == null || !message.isValid()) {
            System.out.println("Сообщение не обработано: " + message);
            return;
        }

        System.out.println("Обработка сообщения: " + message);

        switch (message.getType()) {
            case PLAYER_MOVE:
                // Движение противника
                gameController.handleNetworkInput(message.getData());
                break;

            case POWER_UP:
                // Активация способности противника
                gameController.handleNetworkPowerUp();
                break;

            case PLAYER_JOIN:
                // выбор персонажа противника
                handlePlayerJoin(message.getData(), message.getPlayerId());
                break;

            case ROUND_RESULT:
                // результат раунда от сервера
                handleRoundResult(message.getData());
                break;

            default:
                System.out.println("Неизвестный тип сообщения: " + message.getType());
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

    private void handleRoundResult(String winnerIdStr) {
        try {
            int winnerId = Integer.parseInt(winnerIdStr);
            System.out.println("Получен результат раунда: победитель " + winnerId);
            if (gameController != null) {
                gameController.handleRoundResult(winnerId);
            }
        } catch (NumberFormatException e) {
            System.err.println("Некорректный ID победителя: " + winnerIdStr);
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
            System.out.println("Игрок отключился, игра будет завершена");
        }
    }
    // Метод для GameEngine чтобы отправлять результаты раундов
    public void notifyRoundResult(int winnerId) {
        if (isServer) {
            sendRoundResult(winnerId);
        }
    }
}