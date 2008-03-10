package net.weta.components.communication.tcp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.security.JavaKeystore;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.tcp.client.CommunicationClient;
import net.weta.components.communication.tcp.client.MultiCommunicationClient;
import net.weta.components.communication.tcp.server.CommunicationServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TcpCommunication implements ICommunication {

    private static final Logger LOG = Logger.getLogger(TcpCommunication.class);

    private String _peerName;

    private MessageQueue _messageQueue;

    private boolean _isCommunicationServer;

    private List _servers = new ArrayList();

    private List _serverNames = new ArrayList();

    private String _proxy = null;

    private String _proxyUser = null;

    private String _proxyPassword = null;

    private CommunicationServer _communicationServer;

    private int _id = 0;

    private MultiCommunicationClient _communicationClient;

    private boolean _useProxy = false;

    private int _messageHandleTimeout = 10;

    private int _maxThreadCount = 50;

    private int _maxMessageSize = 1024 * 1024; // 1mb

    private int _connectTimeout = 10;

    private String _keystorePassword;

    private String _keystore;

    private boolean _isSecure;

    private int _maxMessageQueueSize = 2000;

    public TcpCommunication() {
        _messageQueue = new MessageQueue();
        _messageQueue.setMaxSize(_maxMessageQueueSize);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("message count in queue: [" + _messageQueue.size() + "]");
            printStatus();
        }
        synchronized (this) {
            message.setId(_peerName + '_' + (++_id));
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
            if (LOG.isEnabledFor(Level.INFO)) {
                LOG.info("Shutdown the server.");
            }
            _communicationServer.interrupt();
        } else {
            if (LOG.isEnabledFor(Level.INFO)) {
                LOG.info("Shutdown the client.");
            }
            _communicationClient.shutdown();
        }
    }

    public void startup() throws IOException {

        SecurityUtil util = createSecurityUtil();

        if (_isCommunicationServer) {
            String server = (String) _servers.get(0);
            String port = server.substring(server.indexOf(":") + 1, server.length());
            _communicationServer = new CommunicationServer(Integer.parseInt(port), _messageQueue, _maxThreadCount,
                    _connectTimeout, _maxMessageSize, util);
            _communicationServer.start();
        } else {
            if (_servers.size() == _serverNames.size()) {
                String proxyHost = _proxy != null ? _proxy.substring(0, _proxy.indexOf(":")) : "";
                String proxyPort = _proxy != null ? _proxy.substring(_proxy.indexOf(":") + 1, _proxy.length()) : "0";
                List clients = new ArrayList();
                for (int i = 0; i < _servers.size(); i++) {
                    String server = (String) _servers.get(i);
                    String host = server.substring(0, server.indexOf(":"));
                    String port = server.substring(server.indexOf(":") + 1, server.length());
                    CommunicationClient client = new CommunicationClient(_peerName, host, Integer.parseInt(port),
                            proxyHost, Integer.parseInt(proxyPort), _useProxy, _proxyUser, _proxyPassword,
                            _messageQueue, _maxThreadCount, _connectTimeout, _maxMessageSize, (String) _serverNames
                                    .get(i), util);
                    clients.add(client);
                }
                CommunicationClient[] clientArray = (CommunicationClient[]) clients
                        .toArray(new CommunicationClient[clients.size()]);
                _communicationClient = new MultiCommunicationClient(clientArray);
                _communicationClient.start();
            } else {
                throw new IOException("Start failed! Please set for every server the name.");
            }
        }
    }

    private SecurityUtil createSecurityUtil() throws IOException {
        SecurityUtil util = null;
        if (_isSecure) {
            JavaKeystore keystore = new JavaKeystore(new File(_keystore), _keystorePassword);
            util = new SecurityUtil(keystore);
        }
        return util;
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

    public void addServerName(String server) {
        _serverNames.add(server);
    }

    public List getServers() {
        return _servers;
    }

    public List getServerNames() {
        return _serverNames;
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

    public void setKeystorePassword(String keystorePassword) {
        _keystorePassword = keystorePassword;
    }

    public void setKeystore(String keystore) {
        _keystore = keystore;
    }

    public void setIsSecure(boolean isSecure) {
        _isSecure = isSecure;
    }

    /**
     * @return the maxMessageQueueSize
     */
    public int getMaxMessageQueueSize() {
        return _maxMessageQueueSize;
    }

    /**
     * @param maxMessageQueueSize
     *            the maxMessageQueueSize to set
     */
    public void setMaxMessageQueueSize(int maxMessageQueueSize) {
        _maxMessageQueueSize = maxMessageQueueSize;
    }

    public void setProxyUser(String proxyUser) {
        _proxyUser = proxyUser;
    }

    public String getProxyUser() {
        return _proxyUser;
    }

    public void setProxyPassword(String proxyPassword) {
        _proxyPassword = proxyPassword;
    }

    public String getProxyPassword() {
        return _proxyPassword;
    }

    private void printStatus() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long reservedMemory = runtime.totalMemory();
        long used = reservedMemory - freeMemory;
        float percent = 100 * used / maxMemory;
        LOG.info("Memory Usage: [" + (used / (1024 * 1024)) + " MB used of " + (maxMemory / (1024 * 1024))
                + " MB total (" + percent + " %)" + "]");
    }
    
    public List getRegisteredClients() {
        List result = new ArrayList();
        if (_isCommunicationServer) {
            result = _communicationServer.getRegisteredClients();
        } else {
            result = getServerNames();
        }
        return result;
    }
    
    public boolean isConnected(String serverName) {
        boolean result= false;
        if (_isCommunicationServer) {
            result = true;
        } else {
            result = _communicationClient.isConnected(serverName);
        }
        return result;
    }
}
