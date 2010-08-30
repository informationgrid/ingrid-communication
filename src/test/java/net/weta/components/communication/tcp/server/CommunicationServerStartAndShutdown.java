package net.weta.components.communication.tcp.server;

import java.io.IOException;

import net.weta.components.communication.configuration.ServerConfiguration;
import net.weta.components.communication.tcp.TcpCommunication;

public class CommunicationServerStartAndShutdown {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
        
		try {
			TcpCommunication server = new TcpCommunication();
			ServerConfiguration serverConfiguration = new ServerConfiguration();
			serverConfiguration.setName("testserverchen");
			serverConfiguration.setPort(55598);
			server.configure(serverConfiguration);
			server.startup();
			
			Thread.sleep(2000);
			server.shutdown();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
