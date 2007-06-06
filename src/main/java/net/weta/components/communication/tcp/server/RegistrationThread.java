package net.weta.components.communication.tcp.server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.weta.components.communication.security.SecurityUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class RegistrationThread extends Thread {

    private static final Logger LOG = Logger.getLogger(RegistrationThread.class);

    private final Socket _socket;

    private final ICommunicationServer _communicationServer;

    private final int _maxMessageSize;

    private int _connectTimeout;

    private final SecurityUtil _securityUtil;

    public RegistrationThread(Socket socket, ICommunicationServer registration, int maxMessageSize, int connectTimeout,
            SecurityUtil securityUtil) {
        _socket = socket;
        _communicationServer = registration;
        _maxMessageSize = maxMessageSize;
        _connectTimeout = connectTimeout;
        _securityUtil = securityUtil;
    }

    public void run() {
        String peerName;
        try {

            peerName = readPeerName(_socket);
            if (LOG.isEnabledFor(Level.INFO)) {
                LOG.info("receive peerName: [" + peerName + "]");
            }
            if (peerName != null) {
                byte[] bytesToSign = ("" + System.currentTimeMillis()).getBytes();
                if (LOG.isEnabledFor(Level.INFO)) {
                    LOG.info("send bytes for signing to peerName: [" + peerName + "]");
                }
                sendByteArray(_socket, bytesToSign);
                byte[] signedBytes = readByteArray(_socket);
                boolean signatureOk = _securityUtil.verifySignature(peerName, bytesToSign, signedBytes);
                if (signatureOk) {
                    _communicationServer.register(peerName, _socket);
                    if (LOG.isEnabledFor(Level.INFO)) {
                        LOG.info("Signature OK: [" + new String(signedBytes) + "]");
                        LOG.info("Registration successfully for peerName: [" + peerName + "]");
                    }
                } else {
                    if (LOG.isEnabledFor(Level.WARN)) {
                        LOG.warn("Signature invalid: [" + new String(signedBytes) + "]");
                        LOG.warn("Registration failed for peerName: [" + peerName + "]");
                    }
                    writeBoolean(_socket, false);
                }
            } else {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Registration failed, peerName invalid (message too big)).");
                }
                writeBoolean(_socket, false);
                _socket.close();
            }
        } catch (IOException e) {
            LOG.error("client can not register to communication server", e);
            writeBoolean(_socket, false);
            try {
                _socket.close();
            } catch (IOException ioe) {
                LOG.error("can not close socket.", ioe);
            }
        }
    }

    private byte[] readByteArray(Socket socket) throws IOException {
        byte[] bytes = null;
        InputStream inputStream = socket.getInputStream();
        DataInput dataInput = new DataInputStream(inputStream);
        int byteLength = dataInput.readInt();
        if (byteLength > _maxMessageSize) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("new message ignored, message size to big: [" + byteLength + "]");
            }
        } else {
            bytes = new byte[byteLength];
            dataInput.readFully(bytes, 0, bytes.length);
        }
        return bytes;
    }

    private String readPeerName(Socket socket) throws IOException {
        socket.setSoTimeout(_connectTimeout);
        String peerName = null;
        byte[] bytes = readByteArray(socket);
        peerName = bytes != null ? new String(bytes) : null;
        socket.setSoTimeout(0);
        return peerName;
    }

    private void sendByteArray(Socket socket, byte[] bytes) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        dataOutput.writeInt(bytes.length);
        dataOutput.write(bytes);
        dataOutput.flush();
    }

    private void writeBoolean(Socket socket, boolean status) {
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
}
