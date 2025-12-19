package sumogame.network;

import sumogame.controller.GameController;
import sumogame.model.CharacterType;
import sumogame.model.Message;

public class NetworkManager {
    private GameController controller;
    private GameServer server;
    private GameClient client;
    private boolean isServerMode;

    public NetworkManager(GameController controller) {
        this.controller = controller;
    }

    public void startServer() {
        isServerMode = true;

        // Создаем сервер с листенерами
        server = new GameServer(new GameServer.ServerListener() {
            @Override
            public void onClientMessage(Message message) {
                handleMessage(message);
            }

            @Override
            public void onClientConnected(int playerId) {
                System.out.println("Клиент подключился с ID: " + playerId);
                sendGameStart();
            }

            @Override
            public void onClientDisconnected(int playerId) {
                System.out.println("Клиент отключился: " + playerId);
                if (controller != null) {
                    controller.onGameEvent("PLAYER_DISCONNECTED", String.valueOf(playerId));
                }
            }
        });

        server.start();
    }

    public void connectToServer(String address) {
        isServerMode = false;

        // Создаем клиент с листенерами
        client = new GameClient(new GameClient.ClientListener() {
            @Override
            public void onMessageReceived(Message message) {
                handleMessage(message);
            }

            @Override
            public void onConnected(int playerId) {
                System.out.println("Подключены к серверу с ID: " + playerId);
            }

            @Override
            public void onDisconnected() {
                System.out.println("Отключены от сервера");
                if (controller != null) {
                    controller.onGameEvent("PLAYER_DISCONNECTED", "0");
                }
            }
        });

        client.connect(address);
    }

    public void sendPlayerJoin(CharacterType character) {
        Message message = new Message(Message.Type.PLAYER_JOIN, character.name());

        // Отправляем в любом случае (и сервер, и клиент)
        if (isServerMode && server != null) {
            server.sendToClient(message);
        } else if (!isServerMode && client != null) {
            client.sendMessage(message);
        }

        System.out.println("Отправлен выбор персонажа: " + character.name());
    }

    private void handleMessage(Message message) {
        if (controller == null) return;

        switch (message.getType()) {
            case PLAYER_MOVE:
                controller.handleNetworkInput(message.getData());
                break;

            case POWER_UP:
                controller.handleNetworkPowerUp();
                break;

            case GAME_EVENT:
                String[] parts = message.getData().split(":", 2);
                if (parts.length == 2) {
                    controller.onGameEvent(parts[0], parts[1]);
                } else {
                    controller.onGameEvent(message.getData(), "");
                }
                break;

            case GAME_STATE:
                // Для полной синхронизации
                break;

            case PLAYER_JOIN:
                // Получен выбор персонажа от противника
                String characterName = message.getData();
                try {
                    CharacterType opponentCharacter = CharacterType.valueOf(characterName);
                    System.out.println("Противник выбрал персонажа: " + opponentCharacter.getName());

                    // Передаем выбор в GameController
                    if (controller != null) {
                        // Нужен метод в GameController для обновления персонажа противника
                        controller.updateOpponentCharacter(opponentCharacter);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный тип персонажа: " + characterName);
                }
                break;
        }
    }

    public void sendPlayerMove(String direction) {
        Message message = new Message(Message.Type.PLAYER_MOVE, direction);

        if (isServerMode && server != null) {
            server.sendToClient(message);
        } else if (!isServerMode && client != null) {
            client.sendMessage(message);
        }
    }

    public void sendPowerUp() {
        Message message = new Message(Message.Type.POWER_UP, "ACTIVATE");

        if (isServerMode && server != null) {
            server.sendToClient(message);
        } else if (!isServerMode && client != null) {
            client.sendMessage(message);
        }
    }



    private void sendGameStart() {
        if (isServerMode && server != null) {
            Message message = new Message(Message.Type.GAME_EVENT, "GAME_STARTED");
            server.sendToClient(message);

            if (controller != null) {
                controller.onGameEvent("GAME_STARTED", "");
            }
        }
    }

    public void disconnect() {
        if (isServerMode && server != null) {
            server.stop();
        } else if (!isServerMode && client != null) {
            client.disconnect();
        }
    }

    public boolean isConnected() {
        if (isServerMode) {
            return server != null;
        } else {
            return client != null && client.isConnected();
        }
    }
}