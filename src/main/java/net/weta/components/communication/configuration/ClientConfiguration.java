/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package net.weta.components.communication.configuration;

import java.util.ArrayList;
import java.util.List;

public class ClientConfiguration extends Configuration {

	public class ClientConnection {

		private String _serverName;

		private int _serverPort;

		private int _socketTimeout = 10;

		private String _serverIp;

		private int _proxyPort;

		private String _proxyIp;

		private String _proxyPassword;

		private String _proxyUser;

		private String _keystorePassword;

		private String _keystorePath;

		private int _maxMessageSize = 1048576;

		private int _messageThreadCount = 100;

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

	private List<ClientConnection> _clientConnections = new ArrayList<ClientConnection>();

	public List<ClientConnection> getClientConnections() {
		return _clientConnections;
	}
	
	public ClientConnection getClientConnection(int index) {
        return (ClientConnection) _clientConnections.get(index);
    }

	public void setClientConnections(List<ClientConnection> clientConnections) {
		_clientConnections = clientConnections;
	}

	public void addClientConnection(ClientConnection clientConnection) {
		_clientConnections.add(clientConnection);
	}

}
