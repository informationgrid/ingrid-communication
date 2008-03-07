package net.weta.components.communication.tcp.server;

import java.net.Socket;
import java.util.List;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

public interface ICommunicationServer {

    void register(String peerName, Socket socket, IInput in, IOutput out);

    void deregister(String peerName);
    
    List getRegisteredClients();
}
