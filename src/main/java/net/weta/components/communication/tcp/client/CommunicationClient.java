/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.tcp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
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

import de.ingrid.communication.authentication.IHttpProxyConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommunicationClient implements IMessageSender, ICommunicationClient {

    private static final int BUFFER_SIZE = 65535;

    private static final Logger LOG = LogManager.getLogger(CommunicationClient.class);

    private Socket _socket;

    private final String _serverHost;

    private final int _serverPort;

    private final String _proxyServer;

    private final Integer _proxyPort;

    private final MessageQueue _messageQueue;

    private final String _peerName;

    private MessageReaderThread _messageReaderThread;

    private boolean _isConnected = false;

    private boolean _isConnecting = false;

    private final int _maxThreadCount;

    private final int _socketTimeout;

    private final String _serverName;

    private final SecurityUtil _securityUtil;

    private IOutput _out;

    private IInput _in;

    private final int _maxMessageSize;

    private final String _proxyUser;

    private final String _proxyPassword;

    private boolean _shutdown = false;

	private final IHttpProxyConnector _httpProxyConnector;

    public CommunicationClient(String peerName, String serverHost, int serverPort, String proxyServer, int proxyPort,
            String proxyUser, String proxyPassword, MessageQueue messageQueue,
            int maxThreadCount,
            int socketTimeout, int maxMessageSize, String serverName, SecurityUtil securityUtil, IHttpProxyConnector httpProxyConnector) {
        _peerName = peerName;
        _serverHost = serverHost;
        _serverPort = serverPort;
        _proxyServer = proxyServer;
        _proxyPort = new Integer(proxyPort);
        _proxyUser = proxyUser;
        _proxyPassword = proxyPassword;
        _messageQueue = messageQueue;
        _maxThreadCount = maxThreadCount;
        _socketTimeout = socketTimeout;
        _maxMessageSize = maxMessageSize;
        _serverName = serverName;
        _securityUtil = securityUtil;
		_httpProxyConnector = httpProxyConnector;
    }

    public synchronized void connect(String url) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Communication client try to connect...");
        }
        _isConnecting = true;
        _isConnected = false;

        if (_shutdown) {
            if (LOG.isInfoEnabled()) {
                LOG.info("client was explicit disconnected. do not make a connect.");
            }
            _isConnecting = false;
            return;
        }

        try {
            if (_messageReaderThread != null && _messageReaderThread.isAlive()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Previous message reader thread [" + _messageReaderThread.getName() + "] is still running, interruping now, close socket.");
                }
            	_messageReaderThread.interrupt();
                if (LOG.isInfoEnabled()) {
                    LOG.info("MessageReaderThread interrupted successfully.");
                }
            	if (_socket != null && !_socket.isClosed()) {
            		_socket.close();
            	}
                if (LOG.isInfoEnabled()) {
                    LOG.info("Socket closed.");
                }
            }
        	
        	_socket = new Socket();
            if (_proxyServer != null && _proxyPort != null) {
                connectThroughHttpProxy();
            } else {
                connectWithoutProxy();
            }

            _socket.setSoTimeout(_socketTimeout * 1000);
            _out = new Output(new DataOutputStream(new BufferedOutputStream(_socket.getOutputStream(), BUFFER_SIZE)));
            _in = new Input(new DataInputStream(new BufferedInputStream(_socket.getInputStream(), BUFFER_SIZE)),
                    _maxMessageSize);

            byte[] signature = new byte[0];
            if (_securityUtil != null) {
                if (LOG.isInfoEnabled()) {
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

            boolean isRegistered;
			try {
				isRegistered = _in.readBoolean();
			} catch (EOFException e) {
				LOG.warn("Registration to server failed with evidence of existing peer name: " + _peerName);
				isRegistered = false;
			}
            if (isRegistered) {
                _socket.setSoTimeout(0);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Registration to server [" + _serverHost + ":" + _serverPort + "] successfully.");
                }
                _messageReaderThread = new MessageReaderThread(_peerName, _in, _messageQueue, this, _maxThreadCount);
                _messageReaderThread.start();
                synchronized (this) {
                    _isConnected = true;
                    this.notify();
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Registration to server [" + _serverHost + ":" + _serverPort + "] fails.");
                }
                _isConnected = false;
                _socket.close();
            }
        } catch (IOException e) {
            try {
                if (!_socket.isClosed()) {
                    _socket.close();
                }
                _socket = null;
            } catch (Exception e1) {
                // ignore
            }
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
        _socket.connect(new InetSocketAddress(_proxyServer, _proxyPort.intValue()));
        boolean connect = false;
        if (_proxyUser != null && _proxyPassword != null) {
        	connect = _httpProxyConnector.connect(_socket, _serverHost, _serverPort, _proxyUser, _proxyPassword);
        } else {
        	connect = _httpProxyConnector.connect(_socket, _serverHost, _serverPort);
        }
        if(!connect) {
        	throw new IOException("Can not connect through http proxy.");
        }
    }

    public void interrupt() {
        if (_messageReaderThread != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Interrupt thread [" + _messageReaderThread.getName() + "]");
            }
            _messageReaderThread.interrupt();
        }
        disconnect(null);
    }

    public void sendMessage(String peerName, Message message) throws IOException {
    	waitUntilClientIsConnected();
    	if (_isConnected) { 
	    	synchronized (_out) {
	            _out.writeObject(message);
	            _out.flush();
	        }
    	} else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Client is not connected, skip sending message.");
            }
    	}
    }

    private void waitUntilClientIsConnected() throws IOException {
        if (!_isConnected) {
            if (LOG.isInfoEnabled()) {
                LOG.info("client is not connected");
            }
            if (!_isConnecting) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("client is not connecting, starts the connect");
                }
                connect(null);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("client not yet connected but is in connecting process, waiting...");
                }
                try {
                    synchronized (this) {
                        this.wait(_socketTimeout * 1000);
                    }
                } catch (InterruptedException e) {
                    if (LOG.isErrorEnabled()) {
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
        if (LOG.isInfoEnabled()) {
            LOG.info("Disconnect client from server, close the socket.");
        }
        try {
            if ((_socket != null) && !_socket.isClosed()) {
                _socket.close();
            }
        } catch (IOException e) {
            LOG.error("can not close socket", e);
        } finally {
            _isConnected = false;
        }
    }

    public void shutdown() {
        _shutdown = true;
        interrupt();
    }

    public boolean isConnected(String serverName) {
        return _isConnected;
    }
}
