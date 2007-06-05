package net.weta.components.communication.tcp.server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class RegistrationThread extends Thread {

    private static final Logger LOG = Logger.getLogger(RegistrationThread.class);

    private final Socket _socket;

    private final ICommunicationServer _registration;

    private final int _maxMessageSize;

    private int _connectTimeout;

    public RegistrationThread(Socket socket, ICommunicationServer registration, int maxMessageSize, int connectTimeout) {
        _socket = socket;
        _registration = registration;
        _maxMessageSize = maxMessageSize;
        _connectTimeout = connectTimeout;
    }

    public void run() {
        String peerName;
        try {
            peerName = readPeerName(_socket);
            if (peerName != null) {
                _registration.register(peerName, _socket);
            } else {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Registration failed, peerName invalid (message too big)).");
                }
                _socket.close();
            }
        } catch (IOException e) {
            LOG.error("client can not register to communication server", e);
            try {
                _socket.close();
            } catch (IOException ioe) {
                LOG.error("can not close socket.", ioe);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Registration to server successfully.");
        }
    }

    private String readPeerName(Socket socket) throws IOException {
        socket.setSoTimeout(_connectTimeout);
        String peerName = null;
        try {
            InputStream inputStream = socket.getInputStream();
            DataInput dataInput = new DataInputStream(inputStream);
            int byteLength = dataInput.readInt();
            if (byteLength > _maxMessageSize) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("new message ignored, message size to big: [" + byteLength + "]");
                }
            } else {
                byte[] bytes = new byte[byteLength];
                dataInput.readFully(bytes, 0, bytes.length);
                peerName = new String(bytes);
            }
        } catch (SocketTimeoutException e) {
            throw new IOException("timeout while registration.");
        }

        socket.setSoTimeout(0);
        return peerName;
    }
}
