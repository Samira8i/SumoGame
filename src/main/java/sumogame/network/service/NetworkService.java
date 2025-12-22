package sumogame.network.service;

import sumogame.network.message.Message;
/**
 * Интерфейс сетевого сервиса
 * Определяет общий контракт для сервера и клиента
 */

//тут такая пометочка, что у сервера и у клиента по одному методу не реализуется и стоят заглушки
//но все равно как будто бы так чище, ведь вся работа происходит на уровне интерфейса
//то есть благодаря ему мы получаем работу и с клиентом и с сервером на уровне networkmanager
//и убираем дуюлирование! то есть тот же самый вызов sendmessage теперь прописывается 1 раз в manager
public interface NetworkService {
    boolean connect(String address);
    void startServer();
    void sendMessage(Message message);
    void disconnect();
    boolean isConnected();
    int getPlayerId(); // 1 или 2 соответственно
}