package net.weta.components.test;

import java.io.InputStream;
import java.util.Date;
import java.util.Timer;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;

public class TestServer {

  private static final Object _mutex = new Object();

  public static void main(String[] args) throws Exception {

    System.out.println("start communication server...");
    InputStream asStream = TestServer.class.getResourceAsStream("/communication-test-server.properties");
    ICommunication communication = StartCommunication.create(asStream);
    communication.startup();

    System.out.println("create sumService proxy to communicate with the client...");
    ISumService clientSumService = (ISumService) ProxyService.createProxy(communication, ISumService.class, "/test-group:test-client");

    System.out.println("add sumService to answer the client...");
    ReflectMessageHandler messageHandler = new ReflectMessageHandler();
    ISumService serverSumService = new SumService();
    messageHandler.addObjectToCall(ISumService.class, serverSumService);
    communication.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
      messageHandler);



    System.out.println("start sceduler to send sum calls...");
    Timer timer = new Timer("SumCaller", true);
    timer.schedule(new SumCaller(clientSumService), new Date(), 30 * 1000);

    synchronized (_mutex) {
      _mutex.wait();
    }
  }


}
