package net.weta.components.communication.configuration;

public class ServerConfiguration extends Configuration {

	private int _port;

    private int _socketTimeout = 10;
	
	private String _keystorePassword;

	private String _keystorePath;

	private int _maxMessageSize = 1048576;

	private int _messageThreadCount = 10;

	

	public int getPort() {
		return _port;
	}

	public void setPort(int port) {
		_port = port;
	}

	public int getSocketTimeout() {
		return _socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		_socketTimeout = socketTimeout;
	}

	public String getKeystorePassword() {
		return _keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		_keystorePassword = keystorePassword;
	}

	public String getKeystorePath() {
		return _keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		_keystorePath = keystorePath;
	}

	public int getMaxMessageSize() {
		return _maxMessageSize;
	}

	public void setMaxMessageSize(int maxMessageSize) {
		_maxMessageSize = maxMessageSize;
	}

	public int getMessageThreadCount() {
		return _messageThreadCount;
	}

	public void setMessageThreadCount(int messageThreadCount) {
		_messageThreadCount = messageThreadCount;
	}
	
}
