package net.weta.components.communication.tcp.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.tcp.MessageReaderThread;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommunicationServer extends Thread implements ICommunicationServer, IMessageSender {

    static class CommunicationClientInfo {
        private final MessageReaderThread _messageReaderThread;
        private final Socket _socket;
        private final IOutput _out;
        private final String _peerName;

        public CommunicationClientInfo(String peerName, MessageReaderThread messageReaderThread, Socket socket, IOutput out) {
            _peerName = peerName;
            _messageReaderThread = messageReaderThread;
            _socket = socket;
            _out = out;
        }

        public String getPeerName() {
            return _peerName;
        }

        public MessageReaderThread getMessageReaderThread() {
            return _messageReaderThread;
        }

        public Socket getSocket() {
            return _socket;
        }

        public IOutput getOut() {
            return _out;
        }

        @Override
        public int hashCode() {
            return _peerName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return ((CommunicationClientInfo) obj)._peerName.equals(_peerName);
        }

    }
    private static final Logger LOG = Logger.getLogger(CommunicationServer.class);

    private Map<String, CommunicationClientInfo> _clientInfos = new ConcurrentHashMap<String, CommunicationClientInfo>();

    private final int _port;

    private final MessageQueue _messageQueue;

    private ServerSocket _serverSocket;

    private final int _maxThreadCount;

    private int _socketTimeout;

    private final SecurityUtil _securityUtil;

    private int _maxMessageSize;

    public CommunicationServer(int port, MessageQueue messageQueue, int maxThreadCount, int socketTimeout,
            int maxMessageSize, SecurityUtil securityUtil) {
        _port = port;
        _messageQueue = messageQueue;
        _maxThreadCount = maxThreadCount;
        _socketTimeout = socketTimeout;
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
                new RegistrationThread(socket, this, _socketTimeout, _maxMessageSize, _securityUtil).start();
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
        if (_clientInfos.containsKey(peerName)) {
            CommunicationClientInfo cci =  _clientInfos.get(peerName);
        	if (!cci.getSocket().isConnected() && cci.getSocket().getInetAddress().toString().equals(socket.getInetAddress().toString())) {
	        	if (LOG.isEnabledFor(Level.WARN)) {
	                LOG.warn("Registration of new client from ip [" + socket.getRemoteSocketAddress() +
	                        "], client with the same name already registered: [" + peerName + "] from same ip. Try to reconnect.");
	            }
        	} else {
	        	if (LOG.isEnabledFor(Level.WARN)) {
	                LOG.warn("Registration of new client from ip [" + socket.getRemoteSocketAddress() +
	                        "], client with the same name already registered: [" + peerName + "] from ip [" + cci.getSocket().getRemoteSocketAddress() + "]");
	            }
	            try {
	                LOG.info("close socket for duplicate peer: [" + peerName + "] from ip: [" + socket.getRemoteSocketAddress() + "]" );
	                socket.close();
	            } catch (IOException e) {
	                LOG.error("can not close socket for duplicate peer [" + peerName + "] from ip: [" + socket.getRemoteSocketAddress() + "]");
	            }
	            return;
        	}
        }

        LOG.info("Client [" + peerName + "] registered from ip [" + socket.getRemoteSocketAddress() + "]");
        MessageReaderThread thread = new MessageReaderThread(peerName, in, _messageQueue, this, _maxThreadCount);
        thread.setDaemon(true);
        thread.start();
        try {
            CommunicationClientInfo communicationClientInfo = new CommunicationClientInfo(peerName, thread, socket, out);
            _clientInfos.put(peerName, communicationClientInfo);
            out.writeBoolean(true);
            out.flush();
        } catch (IOException e) {
            LOG.error(e);
            thread.interrupt();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("client info count: [" + _clientInfos.size() + "]");
            }
        }
    }

    public synchronized void deregister(String peerName) {
        CommunicationClientInfo info = _clientInfos.get(peerName);
        if (info != null) {
            MessageReaderThread thread = info.getMessageReaderThread();
            if (LOG.isInfoEnabled()) {
                LOG.info("interrupt message reader thread for peer: [" + peerName + "]");
            }
            thread.interrupt();
            Socket socket = info.getSocket();
            try {
                LOG.info("close socket for peer: [" + peerName + "] from ip: [" + socket.getRemoteSocketAddress() + "]" );
                socket.close();
            } catch (IOException e) {
                LOG.error("can not close socket for client [" + peerName + "] from ip: [" + socket.getRemoteSocketAddress() + "]");
            }
            finally {
            	// finally remove the peer from the list
            	// this makes sure, that no registration can take place 
            	// unless the client has completely deregistered 
            	_clientInfos.remove(peerName);
            }
        } else {
            LOG.warn("peername does not exists, skip deregister: " + peerName);
        }
    }

    public void sendMessage(String peerName, Message message) throws IOException {
        CommunicationClientInfo info = _clientInfos.get(peerName);
        if (info != null) {
            IOutput out = info.getOut();
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
        if (LOG.isInfoEnabled()) {
            LOG.info("interupt communication server thread.");
        }
        super.interrupt();
        Set<String> peerNames = _clientInfos.keySet();
        for (String peerName : peerNames) {
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

    public List<String> getRegisteredClients() {
        return new ArrayList<String>(_clientInfos.keySet());
    }

    public boolean isConnected(String url) {
        return _clientInfos.containsKey(url);
    }
}
