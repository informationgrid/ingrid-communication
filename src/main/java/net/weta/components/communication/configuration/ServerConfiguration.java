/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package net.weta.components.communication.configuration;

public class ServerConfiguration extends Configuration {

	private int _port;

    private int _socketTimeout = 10;
	
	private String _keystorePassword;

	private String _keystorePath;

	private int _maxMessageSize = 1048576;

	private int _messageThreadCount = 10;
	
	private long maxClientInfoLifetime = 60 * 10 * 1000;
	

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
	
	public long getMaxClientInfoLifetime() {
		return maxClientInfoLifetime;
	}

	public void setMaxClientInfoLifetime(long maxClientInfoLifetime) {
		this.maxClientInfoLifetime = maxClientInfoLifetime;
	}
	
}
