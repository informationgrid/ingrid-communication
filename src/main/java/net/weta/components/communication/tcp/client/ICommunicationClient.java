package net.weta.components.communication.tcp.client;

public interface ICommunicationClient {

    void shutdown();
    
    boolean isConnected(String serverName);
}
