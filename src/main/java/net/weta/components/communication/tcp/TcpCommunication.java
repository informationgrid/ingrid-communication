package net.weta.components.communication.tcp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.Configuration;
import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.configuration.ClientConfiguration.ClientConnection;
import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.security.JavaKeystore;
import net.weta.components.communication.security.SecurityUtil;
import net.weta.components.communication.tcp.client.CommunicationClient;
import net.weta.components.communication.tcp.client.MultiCommunicationClient;
import net.weta.components.communication.tcp.server.CommunicationServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.ingrid.communication.authentication.BasicSchemeConnector;
import de.ingrid.communication.authentication.IHttpProxyConnector;

public class TcpCommunication implements ICommunication {

	private static final Logger LOG = Logger.getLogger(TcpCommunication.class);

	private String _peerName;

	private MessageQueue _messageQueue;

	private CommunicationServer _communicationServer;

	private int _id = 0;

	private MultiCommunicationClient _communicationClient;

	private Configuration _configuration;

	private boolean _isCommunicationServer;

	private int _messageHandleTimeout;

	public TcpCommunication() {
		_messageQueue = new MessageQueue();
	}

	public void closeConnection(String url) throws IOException {
		if (_isCommunicationServer) {
			_communicationServer.deregister(url);
		} else {
			_communicationClient.interrupt();
		}
	}

	public IMessageQueue getMessageQueue() {
		return _messageQueue;
	}

	public String getPeerName() {
		return _configuration.getName();
	}

	public boolean isSubscribed(String url) throws IllegalArgumentException {
		return true;
	}

	public void sendMessage(Message message, String url) throws IOException, IllegalArgumentException {
		// nothing todo
	}

	public Message sendSyncMessage(Message message, String url) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("message count in queue: [" + _messageQueue.size() + "]");
			printStatus();
		}
		synchronized (this) {
			message.setId(_peerName + '_' + (++_id));
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("send message [" + message.getId() + "] to peer: [" + url + "]");
		}
		if (_isCommunicationServer) {
			_communicationServer.sendMessage(url, message);
		} else {
			_communicationClient.sendMessage(url, message);
		}
		Message answer = _messageQueue.waitForMessage(message.getId(), _messageHandleTimeout);

		answer = answer == null ? new PayloadMessage(new TimeoutException("timeout for answer message ["
				+ message.getId() + "] from [" + url + "]"), "") : answer;
		return answer;
	}

	public void setPeerName(String peerName) {
		_peerName = peerName;
	}

	public void shutdown() {
		if (_isCommunicationServer) {
			if (LOG.isEnabledFor(Level.INFO)) {
				LOG.info("Shutdown/Interrupt the server.");
			}
			_communicationServer.interrupt();
		} else {
			if (LOG.isEnabledFor(Level.INFO)) {
				LOG.info("Shutdown the client.");
			}
			_communicationClient.shutdown();
		}
	}

	public void startup() throws IOException {

		if (_isCommunicationServer) {
			ServerConfiguration serverConfiguration = (ServerConfiguration) _configuration;
			SecurityUtil util = createSecurityUtil(serverConfiguration.getKeystorePath(), serverConfiguration
					.getKeystorePassword());
			_communicationServer = new CommunicationServer(serverConfiguration.getPort(), _messageQueue,
					serverConfiguration.getMessageThreadCount(), serverConfiguration.getSocketTimeout(),
					serverConfiguration.getMaxMessageSize(), util, serverConfiguration.getMaxClientInfoLifetime());
			_communicationServer.start();
		} else {
			// shut down old communication clients
			if (_communicationClient != null) {
				_communicationClient.shutdown();
				_communicationClient = null;
			}
			ClientConfiguration clientConfiguration = (ClientConfiguration) _configuration;
			List<CommunicationClient> clients = new ArrayList<CommunicationClient>();
			for (int i = 0; i < clientConfiguration.getClientConnections().size(); i++) {
				ClientConnection clientConnection = clientConfiguration.getClientConnection(i);
				SecurityUtil util = createSecurityUtil(clientConnection.getKeystorePath(), clientConnection
						.getKeystorePassword());
				// we support only basic authentication through http proxy
				IHttpProxyConnector httpProxyConnector = new BasicSchemeConnector();
				CommunicationClient client = new CommunicationClient(_peerName, clientConnection.getServerIp(),
						clientConnection.getServerPort(), clientConnection.getProxyIp(), clientConnection
								.getProxyPort(), clientConnection.getProxyUser(), clientConnection.getProxyPassword(),
						_messageQueue, clientConnection.getMessageThreadCount(), clientConnection.getSocketTimeout(),
						clientConnection.getMaxMessageSize(), clientConnection.getServerName(), util,
						httpProxyConnector);
				clients.add(client);
			}
			CommunicationClient[] clientArray = (CommunicationClient[]) clients.toArray(new CommunicationClient[clients
					.size()]);
			_communicationClient = new MultiCommunicationClient(clientArray);
			_communicationClient.start();
		}
	}

	private SecurityUtil createSecurityUtil(String keystore, String password) throws IOException {
		SecurityUtil util = null;
		if (keystore != null && password != null) {
			JavaKeystore javaKeystore = new JavaKeystore(new File(keystore), password);
			util = new SecurityUtil(javaKeystore);
		}
		return util;
	}

	public void subscribeGroup(String url) throws IOException {
		// nothing to do
	}

	public void unsubscribeGroup(String url) throws IOException {
		// nothing to do
	}

	private void printStatus() {
		Runtime runtime = Runtime.getRuntime();
		long freeMemory = runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long reservedMemory = runtime.totalMemory();
		long used = reservedMemory - freeMemory;
		float percent = 100 * used / maxMemory;
		LOG.info("Memory Usage: [" + (used / (1024 * 1024)) + " MB used of " + (maxMemory / (1024 * 1024))
				+ " MB total (" + percent + " %)" + "]");
	}

	public List<String> getRegisteredClients() {
		List<String> result = new ArrayList<String>();
		if (_isCommunicationServer) {
			result = _communicationServer.getRegisteredClients();
		} else {
			ClientConfiguration clientConfiguration = (ClientConfiguration) _configuration;
			List<ClientConnection> clientConnections = clientConfiguration.getClientConnections();
			for (int i = 0; i < clientConnections.size(); i++) {
				ClientConnection clientConnection = (ClientConnection) clientConnections.get(i);
				String serverName = clientConnection.getServerName();
				result.add(serverName);
			}
		}
		return result;
	}

	public boolean isConnected(String serverName) {
		boolean result = false;
		if (_isCommunicationServer) {
			result = _communicationServer.isConnected(serverName);
		} else {
			result = _communicationClient.isConnected(serverName);
		}
		return result;
	}

	public void configure(Configuration configuration) {
		_configuration = configuration;
		_isCommunicationServer = _configuration instanceof ServerConfiguration ? true : false;
		_messageQueue.setMaxSize(_configuration.getQueueSize());
		_peerName = _configuration.getName();
		_messageHandleTimeout = _configuration.getHandleTimeout();
	}

	public Configuration getConfiguration() {
		return _configuration;
	}

	public List<String> getServerNames() {
		List<String> list = new ArrayList<String>();
		if (_configuration instanceof ServerConfiguration) {
			ServerConfiguration configuration = (ServerConfiguration) _configuration;
			// a server has no server names, we take the name own name
			String name = configuration.getName();
			list.add(name);
		} else if (_configuration instanceof ClientConfiguration) {
			ClientConfiguration configuration = (ClientConfiguration) _configuration;
			// collect all server names
			List<ClientConnection> clientConnections = configuration.getClientConnections();
			for (Iterator<ClientConnection> iterator = clientConnections.iterator(); iterator.hasNext();) {
				ClientConnection connection = iterator.next();
				String name = connection.getServerName();
				list.add(name);
			}
		}
		return list;
	}
}
