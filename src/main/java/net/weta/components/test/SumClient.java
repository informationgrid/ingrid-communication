/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
