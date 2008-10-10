package net.weta.components.communication.configuration;

public abstract class Configuration {

    private String _name;
    private int _queueSize;
    private int _handleTimeout;

    public String getName() {
        return _name;
    }

    public void setName(String serverName) {
        _name = serverName;
    }

    public void setQueueSize(int queueSize) {
        _queueSize = queueSize;
    }

    public void setHandleTimeout(int handleTimeout) {
        _handleTimeout = handleTimeout;
    }

    public int getQueueSize() {
        return _queueSize;
    }

    public int getHandleTimeout() {
        return _handleTimeout;
    }

}
