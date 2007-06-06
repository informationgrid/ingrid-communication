package net.weta.components.communication.tcp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.tcp.MessageReaderThread;
import net.weta.components.communication.tcp.server.IMessageSender;
import net.weta.components.communication.util.MessageUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommunicationClient implements IMessageSender {

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

    private DataOutputStream _dataOutput;

    private MessageReaderThread _messageReaderThread;

    private boolean _isConnected = false;

    private boolean _wasAlreadyStarted = false;

    private final int _maxThreadCount;

    private final int _maxMessageSize;

    private final int _connectTimeout;

    private final String _serverName;

    private final SecurityUtil _securityUtil;

    public CommunicationClient(String peerName, String serverHost, int serverPort, String proxyServer, int proxyPort,
            boolean useProxy, MessageQueue messageQueue, int maxThreadCount, int maxMessageSize, int connectTimeout,
            String serverName, SecurityUtil securityUtil) {
        _peerName = peerName;
        _serverHost = serverHost;
        _serverPort = serverPort;
        _proxyServer = proxyServer;
        _proxyPort = proxyPort;
        _useProxy = useProxy;
        _messageQueue = messageQueue;
        _maxThreadCount = maxThreadCount;
        _maxMessageSize = maxMessageSize;
        _connectTimeout = connectTimeout;
        _serverName = serverName;
        _securityUtil = securityUtil;
    }

    public synchronized void connect(String url) {
        _isConnected = false;
        if (LOG.isInfoEnabled()) {
            LOG.info("Communication client is connecting...");
        }
        try {
            _socket = new Socket();
            if (_useProxy) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("connect to proxy: " + _proxyServer + ":" + _proxyPort);
                }
                _socket.connect(new InetSocketAddress(_proxyServer, _proxyPort));
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("connect to server: " + _serverHost + ":" + _serverPort);
                }
                _socket.connect(new InetSocketAddress(_serverHost, _serverPort));
            }
            InputStream inputStream = _socket.getInputStream();
            OutputStream outputStream = _socket.getOutputStream();
            _dataOutput = new DataOutputStream(new BufferedOutputStream(outputStream, 65535));

            if (_useProxy) {
                DataInputStream dataInput = new DataInputStream(new BufferedInputStream(inputStream, 65535));
                StringBuffer builder = new StringBuffer();
                builder.append("CONNECT " + _serverHost + ":" + _serverPort + " HTTP/1.1" + CRLF);
                builder.append("HOST: " + _serverHost + ":" + _serverPort + CRLF);
                builder.append(CRLF);

                String string = builder.toString();
                _dataOutput.write(string.getBytes());
                _dataOutput.flush();

                byte[] buffer = new byte[ACCEPT_MESSAGE.getBytes().length];
                dataInput.read(buffer, 0, buffer.length);
                assert ACCEPT_MESSAGE.equals(new String(buffer));
            }

            _dataOutput.writeInt(_peerName.getBytes().length);
            _dataOutput.write(_peerName.getBytes());
            _dataOutput.flush();

            boolean isRegistered = isRegistered(_socket);
            if (isRegistered) {
                if (LOG.isEnabledFor(Level.INFO)) {
                    LOG.info("Registration to server [" + _serverHost + ":" + _serverPort + "] successfully.");
                }
                _messageReaderThread = new MessageReaderThread(_peerName, _socket, _messageQueue, this,
                        _maxThreadCount, _maxMessageSize);
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
            _wasAlreadyStarted = true;
        }
    }

    public void interrupt() {
        _isConnected = false;
        if (_messageReaderThread != null) {
            _messageReaderThread.interrupt();
        }
    }

    public synchronized void sendMessage(String peerName, Message message) throws IOException {
        waitUntilClientIsConnected();
        byte[] bytes = MessageUtil.serialize(message);
        sendByteArray(bytes);
    }

    private void sendByteArray(byte[] bytes) throws IOException {
        _dataOutput.writeInt(bytes.length);
        _dataOutput.write(bytes);
        _dataOutput.flush();
    }

    private void waitUntilClientIsConnected() throws IOException {
        if (!_isConnected) {
            if (_wasAlreadyStarted) {
                connect(null);
            } else {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("client not yet connected, waiting...");
                }
                try {
                    this.wait(_connectTimeout * 1000);
                } catch (InterruptedException e) {
                    if (LOG.isEnabledFor(Level.ERROR)) {
                        LOG.error("Interrupted during wait for connection with server." + e);
                    }
                    throw new IOException("Cannot connect with server: " + e.getMessage());
                }
            }
        }
    }

    private boolean isRegistered(Socket socket) throws IOException {
        socket.setSoTimeout(_connectTimeout * 1000);
        boolean ret = false;
        try {
            InputStream inputStream = socket.getInputStream();
            DataInput dataInput = new DataInputStream(inputStream);
            int byteLength = dataInput.readInt();
            if (byteLength < _maxMessageSize) {
                byte[] bytes = new byte[byteLength];
                dataInput.readFully(bytes, 0, byteLength);
                if (LOG.isEnabledFor(Level.INFO)) {
                    LOG.info("receive byte array for signing...");
                }
                byte[] signature = _securityUtil.computeSignature(_peerName, bytes);
                if (LOG.isEnabledFor(Level.INFO)) {
                    LOG.info("send signature to server...");
                }
                sendByteArray(signature);
                ret = dataInput.readBoolean();
            } else {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("ignore byte array for signing, message size to big: [" + byteLength + "]");
                }
            }

        } catch (SocketTimeoutException e) {
            throw new IOException("timeout while registration.");
        }

        socket.setSoTimeout(0);
        return ret;
    }

    public String getServerName() {
        return _serverName;
    }
}