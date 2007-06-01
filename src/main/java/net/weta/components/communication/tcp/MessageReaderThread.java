package net.weta.components.communication.tcp;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.server.TooManyRunningThreads;
import net.weta.components.communication.tcp.server.IMessageSender;
import net.weta.components.communication.util.MessageUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MessageReaderThread extends Thread {

    protected static final Logger LOG = Logger.getLogger(MessageReaderThread.class);

    private final Socket _socket;

    protected final MessageQueue _messageQueue;

    private DataInputStream _dataInput;

    protected final String _peerName;

    protected final IMessageSender _messageSender;

    protected int _threadCount = 0;

    protected final int _maxThreadCount;

    private final int _maxMessageSize;

    public MessageReaderThread(String peerName, Socket socket, MessageQueue messageQueue, IMessageSender messageSender,
            int maxThreadCount, int maxMessageSize) {
        _peerName = peerName;
        _socket = socket;
        _messageQueue = messageQueue;
        _messageSender = messageSender;
        _maxThreadCount = maxThreadCount;
        _maxMessageSize = maxMessageSize;
    }

    public void run() {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("start to read messages for peer: [" + _peerName + "]");
            }

            InputStream inputStream = _socket.getInputStream();
            _dataInput = new DataInputStream(new BufferedInputStream(inputStream, 65535));
            while (!isInterrupted()) {
                byte[] content = readContent(_dataInput);
                if (content != null) {
                    Message message = MessageUtil.deserialize(content);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("read message [" + message.getId() + "] for client [" + _peerName + "]");
                    }
                    waitForAnswer(message);
                } else {
                    if (LOG.isEnabledFor(Level.WARN)) {
                        LOG.warn("content of new message is invalid (to big?) - new message ignored");
                    }
                }
            }
        } catch (SocketException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("connection shutdown by peer (SocketException): " + _peerName);
            }
            if (_messageSender != null) {
                _messageSender.connect(_peerName);
            }
        } catch (EOFException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("connection shutdown by peer (EOFException): " + _peerName);
            }
            if (_messageSender != null) {
                _messageSender.connect(_peerName);
            }
        } catch (IOException e) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error(e);
            }
        } catch (ClassNotFoundException e) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error(e);
            }
        }
    }

    private void waitForAnswer(final Message message) throws IOException {
        if (_threadCount >= _maxThreadCount) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("message not handled because, max thread count reached: " + _maxThreadCount);
            }
            throw new TooManyRunningThreads("No more threads available.");
        }
        Runnable runnable = new Runnable() {
            public void run() {
                Message answer = _messageQueue.messageEvent(message);
                if (answer != null) {
                    answer.setType("");
                    try {
                        _messageSender.sendMessage(_peerName, answer);
                    } catch (IOException e) {
                        if (LOG.isEnabledFor(Level.WARN)) {
                            LOG.warn("failed to handle message", e);
                        }
                    }
                }
                _threadCount--;
            }
        };

        Thread thread = new Thread(runnable);
        _threadCount++;
        thread.start();
    }

    private byte[] readContent(DataInput dataInput) throws IOException {
        byte[] bytes = null;
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

    public void interrupt() {
        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("Shutdown MessageReaderThread.");
        }
        super.interrupt();
        try {
            _socket.close();
        } catch (Exception e) {
            LOG.error("Error on closing socket.");
        }
    }
}
