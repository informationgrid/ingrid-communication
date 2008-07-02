package net.weta.components.test;

import java.io.InputStream;
import java.util.Date;
import java.util.Timer;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;

public class TestClient {

  private static final Object _mutex = new Object();

  public static void main(String[] args) throws Exception {

    System.out.println("start communication client...");
    InputStream asStream = TestServer.class.getResourceAsStream("/communication-test-client.properties");
    ICommunication communication = StartCommunication.create(asStream);
    communication.startup();

    System.out.println("create sumService proxy to communicate with the server...");
    ISumService serverSumService = (ISumService) ProxyService.createProxy(communication, ISumService.class, "/test-group:test-server");


    System.out.println("add sumService to answer the server...");
    ReflectMessageHandler messageHandler = new ReflectMessageHandler();
    ISumService clientSumService = new SumService();
    messageHandler.addObjectToCall(ISumService.class, clientSumService);
    communication.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
      messageHandler);

    System.out.println("start sceduler to send sum calls...");
    Timer timer = new Timer("SumCall", true);
    timer.schedule(new SumCaller(serverSumService), new Date(), 30 * 1000);

    synchronized (_mutex) {
      _mutex.wait();
    }
  }
}
