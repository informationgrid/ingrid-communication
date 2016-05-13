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
package net.weta.components.test;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TcpServer {

    public static void main(String[] args) {
        try {
            System.out.print("Server start...");
            if ((args.length % 2) != 0) {
                usage();
                System.exit(1);
            }

            HashMap arguments = new HashMap();
            for (int i = 0; i < args.length; i = i + 2) {
                arguments.put(args[i], args[i + 1]);
            }

            int port = 8080;
            if (arguments.containsKey("--port")) {
                port = Integer.parseInt((String) arguments.get("--port"));
            }

            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.setReceiveBufferSize(65535);
            serverSocket.bind(new InetSocketAddress(port));

            System.out.println("[OK]");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("new client connected: " + socket.getRemoteSocketAddress());
                new TcpServerThread(socket).start();
            }
        } catch (Exception e) {
            System.out.println("[FAILED]");

            e.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("TcpServer [--ports <port number>]");
        System.out.println("  --port ... port number (default: 8080)");
    }
}
