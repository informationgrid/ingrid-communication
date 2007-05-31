package net.weta.components.communication.tcp.server;

import java.net.Socket;

public interface ICommunicationServer {

    void register(String peerName, Socket socket);

    void deregister(String peerName);
}
