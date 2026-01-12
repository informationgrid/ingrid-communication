/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.communication.tcp.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import net.weta.components.communication.messaging.AuthenticationMessage;
import net.weta.components.communication.messaging.RegistrationMessage;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.stream.Input;
import net.weta.components.communication.stream.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrationThread extends Thread {

    private static final int BUFFER_SIZE = 65535;

    private static final Logger LOG = LogManager.getLogger(RegistrationThread.class);

    private final Socket _socket;

    private final ICommunicationServer _communicationServer;

    private int _socketTimeout;

    private final SecurityUtil _securityUtil;

    private IInput _in;

    private IOutput _out;

    private final int _maxMessageSize;

    public RegistrationThread(Socket socket, ICommunicationServer registration, int socketTimeout, int maxMessageSize,
            SecurityUtil securityUtil) {
        _socket = socket;
        _communicationServer = registration;
        _socketTimeout = socketTimeout;
        _maxMessageSize = maxMessageSize;
        _securityUtil = securityUtil;
    }

    public void run() {
        String peerName = null;
        try {

            _socket.setSoTimeout(_socketTimeout * 1000);
            _out = new Output(new DataOutputStream(new BufferedOutputStream(_socket.getOutputStream(), BUFFER_SIZE)));
            _in = new Input(new DataInputStream(new BufferedInputStream(_socket.getInputStream(), BUFFER_SIZE)),
                    _maxMessageSize);

            AuthenticationMessage authenticationMessage = null;
            if (_securityUtil != null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Send bytes for signing.");
                }
                authenticationMessage = new AuthenticationMessage(("" + System.currentTimeMillis()).getBytes());
                authenticationMessage.write(_out);
            }

            RegistrationMessage registrationMessage = new RegistrationMessage();
            registrationMessage.read(_in);

            peerName = registrationMessage.getRegistrationName();
            boolean signatureOk = true;
            if (_securityUtil != null) {
                byte[] signature = registrationMessage.getSignature();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Security is enabled, verify signature from peerName: [" + peerName + "] from ip: [" + _socket.getRemoteSocketAddress() + "]");
                }
                signatureOk = _securityUtil.verifySignature(peerName, authenticationMessage.getToken(), signature);
            }

            if (signatureOk) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Registration request successfully for peerName: [" + peerName + "] from ip: [" + _socket.getRemoteSocketAddress() + "]");
                }
                _socket.setSoTimeout(0);
                _communicationServer.register(peerName, _socket, _in, _out);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Registration failed for peerName: [" + peerName + "] from ip: [" + _socket.getRemoteSocketAddress() + "]");
                }
                _out.writeBoolean(false);
                _socket.close();
            }
        } catch (Exception e) {
            LOG.error("Client [" + peerName + "] from " + _socket.getRemoteSocketAddress()+ " can not register to communication server.", e);
            try {
                _out.writeBoolean(false);
                _socket.close();
            } catch (IOException ioe) {
                LOG.error("can not close socket.", ioe);
            }
        }
    }

}
