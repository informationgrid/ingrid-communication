package net.weta.components.communication.configuration;

import java.util.ArrayList;
import java.util.List;

public class ClientConfiguration extends Configuration {

	public class ClientConnection {

		private String _serverName;

		private int _serverPort;

		private int _socketTimeout;

		private String _serverIp;

		private int _proxyPort;

		private String _proxyIp;

		private String _proxyPassword;

		private String _proxyUser;

		private String _keystorePassword;

		private String _keystorePath;

		private int _maxMessageSize;

		private int _messageThreadCount;

		public String getServerName() {
			return _serverName;
		}

		public void setServerName(String serverName) {
			_serverName = serverName;
		}

		public int getServerPort() {
			return _serverPort;
		}

		public void setServerPort(int serverPort) {
			_serverPort = serverPort;
		}

		public int getSocketTimeout() {
			return _socketTimeout;
		}

		public void setSocketTimeout(int socketTimeout) {
			_socketTimeout = socketTimeout;
		}

		public String getServerIp() {
			return _serverIp;
		}

		public void setServerIp(String serverIp) {
			_serverIp = serverIp;
		}

		public int getProxyPort() {
			return _proxyPort;
		}

		public void setProxyPort(int proxyPort) {
			_proxyPort = proxyPort;
		}

		public String getProxyIp() {
			return _proxyIp;
		}

		public void setProxyIp(String proxyIp) {
			_proxyIp = proxyIp;
		}

		public String getProxyPassword() {
			return _proxyPassword;
		}

		public void setProxyPassword(String proxyPassword) {
			_proxyPassword = proxyPassword;
		}

		public String getProxyUser() {
			return _proxyUser;
		}

		public void setProxyUser(String proxyUser) {
			_proxyUser = proxyUser;
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

	private List _clientConnections = new ArrayList();

	public List getClientConnections() {
		return _clientConnections;
	}

	public void setClientConnections(List clientConnections) {
		_clientConnections = clientConnections;
	}

	public void addClientConnection(ClientConnection clientConnection) {
		_clientConnections.add(clientConnection);
	}

}
