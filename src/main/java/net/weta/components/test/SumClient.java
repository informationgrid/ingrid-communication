package net.weta.components.test;

import java.io.InputStream;
import java.util.Date;
import java.util.Timer;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;

public class SumClient {

  private static final Object _mutex = new Object();

  public static final String SERVER_PEER_NAME = "/test-group:subtract-server";

  public static void main(String[] args) throws Exception {

    System.out.println("start communication client...");
    InputStream asStream = SubtractServer.class.getResourceAsStream("/communication-test-client.xml");
    ICommunication communication = StartCommunication.create(asStream);
    communication.startup();

    System.out.println("create subtractService proxy to communicate with the server...");
    ISubtractService subtractService = (ISubtractService) ProxyService.createProxy(communication, ISubtractService.class, SERVER_PEER_NAME);


    System.out.println("add sumService to answer the server...");
    ReflectMessageHandler messageHandler = new ReflectMessageHandler();
    ISumService clientSumService = new SumService();
    messageHandler.addObjectToCall(ISumService.class, clientSumService);
    communication.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
      messageHandler);

    System.out.println("start sceduler to send subtract calls...");
    Timer timer = new Timer(true);
    timer.schedule(new ComputeCaller(subtractService, SERVER_PEER_NAME), new Date(), 30 * 1000);

    synchronized (_mutex) {
      _mutex.wait();
    }
  }
}
