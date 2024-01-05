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
package net.weta.components.test;

import java.io.InputStream;
import java.util.Date;
import java.util.Timer;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;

public class SubtractServer {

  public static final String CLIENT_PEER_NAME = "/test-group:sum-client";

  private static final Object _mutex = new Object();

  public static void main(String[] args) throws Exception {

    System.out.println("start communication server...");
    InputStream asStream = SubtractServer.class.getResourceAsStream("/communication-test-server.xml");
    ICommunication communication = StartCommunication.create(asStream);
    communication.startup();

    System.out.println("create sumService proxy to communicate with the client...");
    ISumService clientSumService = (ISumService) ProxyService.createProxy(communication, ISumService.class, CLIENT_PEER_NAME);

    System.out.println("add subtractService to answer the client...");
    ReflectMessageHandler messageHandler = new ReflectMessageHandler();
    ISubtractService subtractService = new SubtractService();
    messageHandler.addObjectToCall(ISubtractService.class, subtractService);
    communication.getMessageQueue().getProcessorRegistry().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE,
      messageHandler);


    System.out.println("start sceduler to send sum calls...");
    Timer timer = new Timer(true);
    timer.schedule(new ComputeCaller(clientSumService, CLIENT_PEER_NAME), new Date(), 30 * 1000);

    synchronized (_mutex) {
      _mutex.wait();
    }
  }


}
