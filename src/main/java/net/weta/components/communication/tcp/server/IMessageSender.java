package net.weta.components.communication.tcp.server;

import java.io.IOException;

import net.weta.components.communication.messaging.Message;

public interface IMessageSender {

    void sendMessage(String peerName, Message message) throws IOException;

    void connect(String url);
    
    void disconnect(String url);

    boolean isConnected(String url);

}
