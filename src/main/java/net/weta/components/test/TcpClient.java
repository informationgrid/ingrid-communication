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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import jakarta.xml.bind.DatatypeConverter;

public class TcpClient {

    private static final String CRLF = "\r\n";

    private static final String ACCEPT_MESSAGE = "HTTP/1.0 200 Connection established" + CRLF + CRLF;

    private static final String SIMPLE_ACCEPT_MESSAGE = "200 Connection established" + CRLF + CRLF;

    public static void main(String[] args) throws Exception {
        boolean useProxy = false;
        if ((args.length % 2) != 0) {
            usage();
            System.exit(1);
        }

        HashMap arguments = new HashMap();
        for (int i = 0; i < args.length; i = i + 2) {
            arguments.put(args[i], args[i + 1]);
        }

        int port = -1;
        port = extractPort(arguments, port);
        String host = "";
        host = extractHost(arguments, host);
        String proxyHost = "";
        proxyHost = extractProxyHost(arguments, proxyHost);
        int proxyPort = 8080;
        proxyPort = extractProxyPort(arguments, proxyPort);
        int maxMessages = 15000;
        maxMessages = extractMaxMessages(arguments, maxMessages);
        String userName = "";
        userName = extractUserName(arguments, userName);
        String password = "";
        password = extractPassword(arguments, password);

        if (!proxyHost.trim().equals("")) {
            useProxy = true;
        }

        Socket socket = new Socket();

        if (useProxy) {
            System.out.println("try to connect to proxy...");
            socket.connect(new InetSocketAddress(proxyHost, proxyPort));
            System.out.println("...connected to proxy");
        } else {
            System.out.println("try to connect to server...");
            socket.connect(new InetSocketAddress(host, port));
            System.out.println("...connected to server");
        }

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        DataInputStream dataInput = new DataInputStream(new BufferedInputStream(inputStream, 65535));
        DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(outputStream, 65535));

        if (useProxy) {

            // ISA PATCH
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("http.auth.digest.validateProxy", "true");
            System.getProperties().put("http.auth.digest.validateServer", "true");
            // ISA PATCH

            StringBuffer builder = new StringBuffer();
            String authString = userName + ":" + password;
            String auth = "Basic " + DatatypeConverter.printBase64Binary(authString.getBytes("UTF-8"));
            builder.append("CONNECT " + host + ":" + port + " HTTP/1.1" + CRLF);
            builder.append("HOST: " + host + ":" + port + CRLF);
            builder.append(("Proxy-Authorization: " + auth + CRLF));
            builder.append(CRLF);

            String string = builder.toString();
            System.out.println("connected to host over proxy...");
            System.out.println("---");
            System.out.println(string);
            System.out.println("---");
            dataOutput.write(string.getBytes());
            dataOutput.flush();

            System.out.println("read accept message from proxy...");

            byte[] bytes = new byte[ACCEPT_MESSAGE.length()];
            while ((dataInput.read(bytes, 0, bytes.length)) != -1) {
                String readedString = new String(bytes);
                System.out.println(readedString);
                if (readedString.toLowerCase().indexOf(SIMPLE_ACCEPT_MESSAGE.toLowerCase()) > -1) {
                    System.out.println("accept message found: '" + SIMPLE_ACCEPT_MESSAGE + "'. Break the loop.");
                    break;
                }
                bytes = new byte[ACCEPT_MESSAGE.length()];
            }
            // assert ACCEPT_MESSAGE.equals(new String(buffer));
        }

        int index = 1;
        int counter = 1;
        long start = System.currentTimeMillis();

        System.out.println("connect to server finished, starts with communication");
        while (index++ < maxMessages) {
            int byteLength = dataInput.readInt();
            byte[] bytes = new byte[byteLength];
            dataInput.readFully(bytes, 0, byteLength);

            if ((counter++ % 1000) == 0) {
                System.out.println(counter + " msg received with size [" + byteLength / 1024.0 + " kb] and speed ["
                        + counter / ((System.currentTimeMillis() - start) / 1000.0) + " msg/s]");
                counter = 1;
                start = System.currentTimeMillis();
            }

            dataOutput.writeInt(bytes.length);
            dataOutput.write(bytes);
            dataOutput.flush();
        }

        dataOutput.writeInt(-1);
        dataOutput.flush();
        Thread.sleep(2000);
        socket.close();
        System.out.println("client end");

    }

    private static String extractPassword(HashMap arguments, String password) {
        if (arguments.containsKey("--password")) {
            password = (String) arguments.get("--password");
        }
        return password;
    }

    private static String extractUserName(HashMap arguments, String userName) {
        if (arguments.containsKey("--userName")) {
            userName = (String) arguments.get("--userName");
        }
        return userName;
    }

    private static int extractMaxMessages(HashMap arguments, int maxMessages) {
        if (arguments.containsKey("--maxMessages")) {
            maxMessages = Integer.parseInt((String) arguments.get("--maxMessages"));
        }
        return maxMessages;
    }

    private static int extractProxyPort(HashMap arguments, int proxyPort) {
        if (arguments.containsKey("--proxyPort")) {
            proxyPort = Integer.parseInt((String) arguments.get("--proxyPort"));
        }
        return proxyPort;
    }

    private static String extractProxyHost(HashMap arguments, String proxyHost) {
        if (arguments.containsKey("--proxyHost")) {
            proxyHost = (String) arguments.get("--proxyHost");
        }
        return proxyHost;
    }

    private static String extractHost(HashMap arguments, String host) {
        if (arguments.containsKey("--host")) {
            host = (String) arguments.get("--host");
        } else {
            usage();
            System.exit(1);
        }
        return host;
    }

    private static int extractPort(HashMap arguments, int port) {
        if (arguments.containsKey("--port")) {
            port = Integer.parseInt((String) arguments.get("--port"));
        } else {
            usage();
            System.exit(1);
        }
        return port;
    }

    private static void usage() {
        System.out.println("TcpClient --port <port number>");
        System.out.println("  --port ... port number");
        System.out.println("  --host ... host");
        System.out.println("  --proxyHost ... host");
        System.out.println("  --proxyPort ... port number");
        System.out.println("  --maxMessages ... messages");
    }

}
