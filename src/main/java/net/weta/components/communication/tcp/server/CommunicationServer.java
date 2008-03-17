package net.weta.components.communication.tcp.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.tcp.MessageReaderThread;
import net.weta.components.communication.tcp.TcpCommunication;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommunicationServer extends Thread implements ICommunicationServer, IMessageSender {

    private static final Logger LOG = Logger.getLogger(CommunicationServer.class);

    private Map _messageReaderMap = new HashMap();

    private Map _outputStreamMap = new HashMap();

    private Map _sockets = new HashMap();

    private final int _port;

    private final MessageQueue _messageQueue;

    private ServerSocket _serverSocket;

    private final int _maxThreadCount;

    private int _connectTimeout;

    private final SecurityUtil _securityUtil;

    private int _maxMessageSize;

    public CommunicationServer(int port, MessageQueue messageQueue, int maxThreadCount, int connectTimeout,
            int maxMessageSize, SecurityUtil securityUtil) {
        _port = port;
        _messageQueue = messageQueue;
        _maxThreadCount = maxThreadCount;
        _connectTimeout = connectTimeout;
        _maxMessageSize = maxMessageSize;
        _securityUtil = securityUtil;
    }

    public void run() {
        _serverSocket = null;
        try {
            _serverSocket = new ServerSocket(_port);
            LOG.info("Communication server is startet...");
            while (!isInterrupted()) {
                Socket socket = _serverSocket.accept();
                LOG.info("new client is connected from ip: [" + socket.getRemoteSocketAddress() +
                        "], start registration...");
                new RegistrationThread(socket, this, _connectTimeout, _maxMessageSize, _securityUtil).start();
            }
        } catch (BindException e) {
            LOG.error(e.getMessage() + " " + _port);
        } catch (SocketException e) {
            LOG.info("normal connection shutdown (SocketException): ");
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public synchronized void register(String peerName, Socket socket, IInput in, IOutput out) {
        if (_messageReaderMap.containsKey(peerName)) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Registration of new client from ip [" + socket.getRemoteSocketAddress() +
                        "], client with the same name already registered: [" + peerName + "]");
            }
            deregister(peerName);
        }

        LOG.info("Client [" + peerName + "] registered from ip [" + socket.getRemoteSocketAddress() + "]");
        MessageReaderThread thread = new MessageReaderThread(peerName, in, _messageQueue, this, _maxThreadCount);
        thread.setDaemon(true);
        thread.start();
        try {
            _messageReaderMap.put(peerName, thread);
            _outputStreamMap.put(peerName, out);
            _sockets.put(peerName, socket);
            out.writeBoolean(true);
            out.flush();
        } catch (IOException e) {
            LOG.error(e);
            thread.interrupt();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("message reader count: [" + _messageReaderMap.size() + "]");
                LOG.debug("stream count: [" + _outputStreamMap.size() + "]");
                LOG.debug("socket count: [" + _sockets.size() + "]");
            }
        }
    }

    public void deregister(String peerName) {
        _outputStreamMap.remove(peerName);
        MessageReaderThread thread = (MessageReaderThread) _messageReaderMap.remove(peerName);
        if (thread != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("shutdown peer connection: [" + peerName + "]");
            }
            thread.interrupt();
        }
        Socket socket = (Socket) _sockets.remove(peerName);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOG.error("can not close socket for client [" + peerName + "]");
        }
    }

    public void sendMessage(String peerName, Message message) throws IOException {
        IOutput out = (IOutput) _outputStreamMap.get(peerName);
        if (out != null) {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
            }
        } else {
            LOG.warn("communication partner unknown, message not sent to: " + peerName);
        }
    }

    public IMessageQueue getMessageQueue() {
        return _messageQueue;
    }

    public void interrupt() {
        super.interrupt();
        Set peerNames = _messageReaderMap.keySet();
        String[] peerNameArray = (String[]) peerNames.toArray(new String[peerNames.size()]);
        for (int i = 0; i < peerNameArray.length; i++) {
            String peerName = peerNameArray[i];
            deregister(peerName);
        }
        try {
            if (_serverSocket != null) {
                _serverSocket.close();
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public void connect(String url) {
        deregister(url);
    }

    public void disconnect(String url) {
        deregister(url);
    }

    public static void main(String[] args) throws IOException {
        TcpCommunication communication = new TcpCommunication();
        communication.setIsSecure(false);
        communication.setIsCommunicationServer(true);
        communication.addServer("127.0.0.1:55555");
        communication.addServerName("/101tec-group:ibus");
        communication.startup();
    }

    public List getRegisteredClients() {
        return new ArrayList(_messageReaderMap.keySet());
    }
}
