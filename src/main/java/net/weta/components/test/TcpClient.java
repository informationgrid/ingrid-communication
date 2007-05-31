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

public class TcpClient {

    private static final String CRLF = "\r\n";

    private static final String ACCEPT_MESSAGE = "HTTP/1.0 200 Connection established" + CRLF + CRLF;

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
            StringBuffer builder = new StringBuffer();
            builder.append("CONNECT " + host + ":" + port + " HTTP/1.1" + CRLF);
            builder.append("HOST: " + host + ":" + port + CRLF);
            builder.append(CRLF);

            String string = builder.toString();
            System.out.println("connected to host over proxy...");
            dataOutput.write(string.getBytes());
            dataOutput.flush();

            byte[] buffer = new byte[ACCEPT_MESSAGE.getBytes().length];
            System.out.println("read accept message from proxy...");
            dataInput.read(buffer, 0, buffer.length);
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
