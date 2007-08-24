package net.weta.components.communication.tcp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.weta.components.communication.messaging.AuthenticationMessage;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.RegistrationMessage;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.stream.Input;
import net.weta.components.communication.stream.Output;
import net.weta.components.communication.tcp.MessageReaderThread;
import net.weta.components.communication.tcp.server.IMessageSender;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommunicationClient implements IMessageSender {

    private static final int BUFFER_SIZE = 65535;

    private static final Logger LOG = Logger.getLogger(CommunicationClient.class);

    private static final String CRLF = "\r\n";

    private static final String ACCEPT_MESSAGE = "HTTP/1.0 200 Connection established" + CRLF + CRLF;

    private Socket _socket;

    private final String _serverHost;

    private final int _serverPort;

    private final String _proxyServer;

    private final int _proxyPort;

    private final MessageQueue _messageQueue;

    private final boolean _useProxy;

    private final String _peerName;

    private MessageReaderThread _messageReaderThread;

    private boolean _isConnected = false;

    private boolean _isConnecting = false;

    private final int _maxThreadCount;

    private final int _connectTimeout;

    private final String _serverName;

    private final SecurityUtil _securityUtil;

    private IOutput _out;

    private IInput _in;

    private final int _maxMessageSize;

    private final String _proxyUser;

    private final String _proxyPassword;

    public CommunicationClient(String peerName, String serverHost, int serverPort, String proxyServer, int proxyPort,
            boolean useProxy, String proxyUser, String proxyPassword, MessageQueue messageQueue, int maxThreadCount,
            int connectTimeout, int maxMessageSize, String serverName, SecurityUtil securityUtil) {
        _peerName = peerName;
        _serverHost = serverHost;
        _serverPort = serverPort;
        _proxyServer = proxyServer;
        _proxyPort = proxyPort;
        _useProxy = useProxy;
        _proxyUser = proxyUser;
        _proxyPassword = proxyPassword;
        _messageQueue = messageQueue;
        _maxThreadCount = maxThreadCount;
        _connectTimeout = connectTimeout;
        _maxMessageSize = maxMessageSize;
        _serverName = serverName;
        _securityUtil = securityUtil;
    }

    public synchronized void connect(String url) {
        _isConnecting = true;
        _isConnected = false;

        if (LOG.isInfoEnabled()) {
            LOG.info("Communication client is connecting...");
        }
        try {
            _socket = new Socket();
            if (_useProxy) {
                connectThroughHttpProxy();
            } else {
                connectWithoutProxy();
            }

            _socket.setSoTimeout(_connectTimeout * 1000);
            _out = new Output(new DataOutputStream(new BufferedOutputStream(_socket.getOutputStream(), BUFFER_SIZE)));
            _in = new Input(new DataInputStream(new BufferedInputStream(_socket.getInputStream(), BUFFER_SIZE)),
                    _maxMessageSize);

            byte[] signature = new byte[0];
            if (_securityUtil != null) {
                if (LOG.isEnabledFor(Level.INFO)) {
                    LOG.info("Begin to read authentication token for signing.");
                }
                AuthenticationMessage message = new AuthenticationMessage(new byte[0]);
                message.read(_in);
                byte[] token = message.getToken();
                signature = _securityUtil.computeSignature(_peerName, token);
            }

            RegistrationMessage registrationMessage = new RegistrationMessage();
            registrationMessage.setRegistrationName(_peerName);
            registrationMessage.setSignature(signature);
            registrationMessage.write(_out);

            boolean isRegistered = _in.readBoolean();
            if (isRegistered) {
                _socket.setSoTimeout(0);
                if (LOG.isEnabledFor(Level.INFO)) {
                    LOG.info("Registration to server [" + _serverHost + ":" + _serverPort + "] successfully.");
                }
                _messageReaderThread = new MessageReaderThread(_peerName, _in, _messageQueue, this, _maxThreadCount);
                _messageReaderThread.start();
                synchronized (this) {
                    _isConnected = true;
                    this.notify();
                }
            } else {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Registration to server [" + _serverHost + ":" + _serverPort + "] fails.");
                }
                _isConnected = false;
                _socket.close();
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        } finally {
            _isConnecting = false;
        }
    }

    private void connectWithoutProxy() throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("connect to server: " + _serverHost + ":" + _serverPort);
        }
        _socket.connect(new InetSocketAddress(_serverHost, _serverPort));
    }

    private void connectThroughHttpProxy() throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("connect to proxy: " + _proxyServer + ":" + _proxyPort);
        }
        _socket.connect(new InetSocketAddress(_proxyServer, _proxyPort));
        InputStream inputStream = _socket.getInputStream();
        OutputStream outputStream = _socket.getOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(outputStream, 65535));

        DataInputStream dataInput = new DataInputStream(new BufferedInputStream(inputStream, 65535));
        StringBuffer builder = new StringBuffer();
        String authString = _proxyUser + ":" + _proxyPassword;
        String auth = "Basic " + new String(Base64.encodeBase64(authString.getBytes()));
        builder.append("CONNECT " + _serverHost + ":" + _serverPort + " HTTP/1.1" + CRLF);
        builder.append("HOST: " + _serverHost + ":" + _serverPort + CRLF);
        if (!"".equals(_proxyUser) && !"".equals(_proxyPassword)) {
            builder.append(("Proxy-Authorization: " + auth + CRLF));
        }
        builder.append(CRLF);

        String string = builder.toString();
        dataOutput.write(string.getBytes());
        dataOutput.flush();

        byte[] buffer = new byte[ACCEPT_MESSAGE.getBytes().length];
        if (LOG.isInfoEnabled()) {
            LOG.info("read accept message from proxy starts...");
        }
        while ((dataInput.read(buffer, 0, buffer.length)) != -1) {
            if (LOG.isInfoEnabled()) {
                LOG.info(new String(buffer));
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("read accept message from proxy ends...");
        }
    }

    public void interrupt() {
        if (_messageReaderThread != null) {
            _messageReaderThread.interrupt();
        }
        disconnect(null);
    }

    public void sendMessage(String peerName, Message message) throws IOException {
        waitUntilClientIsConnected();
        synchronized (_out) {
            _out.writeObject(message);
            _out.flush();
        }
    }

    private void waitUntilClientIsConnected() throws IOException {
        if (!_isConnected) {
            if (!_isConnecting) {
                connect(null);
            } else {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("client not yet connected, waiting...");
                }
                try {
                    synchronized (this) {
                        this.wait(_connectTimeout * 1000);
                    }
                } catch (InterruptedException e) {
                    if (LOG.isEnabledFor(Level.ERROR)) {
                        LOG.error("Interrupted during wait for connection with server." + e);
                    }
                    throw new IOException("Cannot connect with server: " + e.getMessage());
                }
            }
        }
    }

    public String getServerName() {
        return _serverName;
    }

    public void disconnect(String url) {
        try {
            _socket.close();
        } catch (IOException e) {
            LOG.error("can not close socket", e);
        } finally {
            _isConnected = false;
        }
    }
}