package net.weta.components.communication.tcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.tcp.client.CommunicationClient;
import net.weta.components.communication.tcp.client.MultiCommunicationClient;
import net.weta.components.communication.tcp.server.CommunicationServer;

import org.apache.log4j.Logger;

public class TcpCommunication implements ICommunication {

    private static final Logger LOG = Logger.getLogger(TcpCommunication.class);

    private String _peerName;

    private MessageQueue _messageQueue;

    private boolean _isCommunicationServer;

    private List _servers = new ArrayList();

    private String _proxy = null;

    private CommunicationServer _communicationServer;

    private int _id = 0;

    private MultiCommunicationClient _communicationClient;

    private boolean _useProxy = false;

    private int _messageHandleTimeout = 10;

    private int _maxThreadCount = 50;

    private int _maxMessageSize = 1024 * 1024; // 1mb

    private int _connectTimeout = 10;

    public TcpCommunication() {
        _messageQueue = new MessageQueue();
    }

    public void closeConnection(String url) throws IOException {
        if (_isCommunicationServer) {
            _communicationServer.deregister(url);
        } else {
            _communicationClient.interrupt();
        }
    }

    public IMessageQueue getMessageQueue() {
        return _messageQueue;
    }

    public String getPeerName() {
        return _peerName;
    }

    public boolean isSubscribed(String url) throws IllegalArgumentException {
        return true;
    }

    public void sendMessage(Message message, String url) throws IOException, IllegalArgumentException {
        // nothing todo
    }

    public Message sendSyncMessage(Message message, String url) throws IOException {
        synchronized (this) {
            message.setId(++_id);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("send message [" + message.getId() + "] to peer: [" + url + "]");
        }
        if (_isCommunicationServer) {
            _communicationServer.sendMessage(url, message);
        } else {
            _communicationClient.sendMessage(url, message);
        }
        Message answer = _messageQueue.waitForMessage(message.getId(), _messageHandleTimeout);

        answer = answer == null ? new PayloadMessage(new TimeoutException("timeout for answer message ["
                + message.getId() + "] from [" + url + "]"), "") : answer;
        return answer;
    }

    public void setPeerName(String peerName) {
        _peerName = peerName;
    }

    public void shutdown() {
        if (_isCommunicationServer) {
            _communicationServer.interrupt();
        } else {
            _communicationClient.interrupt();
        }
    }

    public void startup() throws IOException {
        if (_isCommunicationServer) {
            String server = (String) _servers.get(0);
            String port = server.substring(server.indexOf(":") + 1, server.length());
            _communicationServer = new CommunicationServer(Integer.parseInt(port), _messageQueue, _maxThreadCount,
                    _maxMessageSize, _connectTimeout);
            _communicationServer.start();
        } else {
            String proxyHost = _proxy != null ? _proxy.substring(0, _proxy.indexOf(":")) : "";
            String proxyPort = _proxy != null ? _proxy.substring(_proxy.indexOf(":") + 1, _proxy.length()) : "0";
            List clients = new ArrayList();
            for (int i = 0; i < _servers.size(); i++) {
                String server = (String) _servers.get(0);
                String host = server.substring(0, server.indexOf(":"));
                String port = server.substring(server.indexOf(":") + 1, server.length());
                CommunicationClient client = new CommunicationClient(_peerName, host, Integer.parseInt(port),
                        proxyHost, Integer.parseInt(proxyPort), _useProxy, _messageQueue, _maxThreadCount,
                        _maxMessageSize, _connectTimeout);
                clients.add(client);
            }
            CommunicationClient[] clientArray = (CommunicationClient[]) clients.toArray(new CommunicationClient[clients
                    .size()]);
            _communicationClient = new MultiCommunicationClient(clientArray);
            _communicationClient.start();
        }
    }

    public void subscribeGroup(String url) throws IOException {
        // nothing to do
    }

    public void unsubscribeGroup(String url) throws IOException {
        // nothing to do
    }

    public void setIsCommunicationServer(boolean isCommunicationServer) {
        _isCommunicationServer = isCommunicationServer;
    }

    public void addServer(String server) {
        _servers.add(server);
    }

    public void setProxy(String proxy) {
        _proxy = proxy;
    }

    public void setUseProxy(boolean useProxy) {
        _useProxy = useProxy;
    }

    public void setMessageHandleTimeout(int timeout) {
        _messageHandleTimeout = timeout;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        _maxThreadCount = maxThreadCount;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        _maxMessageSize = maxMessageSize;
    }

    /**
     * @return the connectTimeout
     */
    public int getConnectTimeout() {
        return _connectTimeout;
    }

    /**
     * @param connectTimeout
     *            the connectTimeout to set
     */
    public void setConnectTimeout(int connectTimeout) {
        _connectTimeout = connectTimeout;
    }
}
