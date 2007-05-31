package net.weta.components.communication.tcp.server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.tcp.MessageReaderThread;
import net.weta.components.communication.util.MessageUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommunicationServer extends Thread implements ICommunicationServer, IMessageSender {

    private static final Logger LOG = Logger.getLogger(CommunicationServer.class);

    private Map _messageReaderMap = new HashMap();

    private Map _outputStreamMap = new HashMap();

    private final int _port;

    private final MessageQueue _messageQueue;

    private ServerSocket _serverSocket;

    private final int _maxThreadCount;

    private final int _maxMessageSize;

    private int _connectTimeout;

    public CommunicationServer(int port, MessageQueue messageQueue, int maxThreadCount, int maxMessageSize,
            int connectTimeout) {
        _port = port;
        _messageQueue = messageQueue;
        _maxThreadCount = maxThreadCount;
        _maxMessageSize = maxMessageSize;
        _connectTimeout = connectTimeout;
    }

    public void run() {
        _serverSocket = null;
        try {
            _serverSocket = new ServerSocket(_port);
            LOG.info("Communication server is startet...");
            while (!isInterrupted()) {
                Socket socket = _serverSocket.accept();
                new RegistrationThread(socket, this, _maxMessageSize, _connectTimeout).start();
            }
        } catch (BindException e) {
            LOG.error(e.getMessage() + " " + _port);
        } catch (SocketException e) {
            LOG.info("normal connection shutdown (SocketException): ");
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public synchronized void register(String peerName, Socket socket) {
        if (_messageReaderMap.containsKey(peerName)) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Registration of new client from ip [" + socket.getRemoteSocketAddress()
                        + "] disallow, client with the same name already registered: [" + peerName + "]");
            }
            writeRegisteredStatus(socket, false);
        } else {
            LOG.info("new client [" + peerName + "] registered from ip [" + socket.getRemoteSocketAddress() + "]");
            writeRegisteredStatus(socket, true);
            MessageReaderThread thread = new MessageReaderThread(peerName, socket, _messageQueue, this,
                    _maxThreadCount, _maxMessageSize);
            thread.start();
            try {
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(outputStream, 65535));
                _messageReaderMap.put(peerName, thread);
                _outputStreamMap.put(peerName, dataOutput);
            } catch (IOException e) {
                LOG.error(e);
                thread.interrupt();
            }
        }
    }

    private void writeRegisteredStatus(Socket socket, boolean status) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);
            stream.writeBoolean(status);
            stream.flush();
        } catch (IOException e) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error("can not post regsiter status to client", e);
            }
        }
    }

    public void deregister(String peerName) {
        _outputStreamMap.remove(peerName);
        MessageReaderThread thread = (MessageReaderThread) _messageReaderMap.remove(peerName);
        if (thread != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("shutdown peer: [" + peerName + "]");
            }
            thread.interrupt();
        }
    }

    public synchronized void sendMessage(String peerName, Message message) throws IOException {
        DataOutputStream dataOutput = (DataOutputStream) _outputStreamMap.get(peerName);
        if (dataOutput != null) {
            byte[] bytes = MessageUtil.serialize(message);
            dataOutput.writeInt(bytes.length);
            dataOutput.write(bytes);
            dataOutput.flush();
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
        Iterator iterator = peerNames.iterator();
        while (iterator.hasNext()) {
            String peerName = (String) iterator.next();
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
}
