package net.weta.components.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class TcpServerThread extends Thread {

    private final Socket _socket;

    public TcpServerThread(Socket socket) {
        _socket = socket;
    }

    public void run() {
        try {
            String message = "hello world!";
            int count = 0;
            while (true) {
                if (count > 15) {
                    message = "hello world!";
                    count = 0;
                }
                message = message + message;
                float messageSize = (float) (message.getBytes().length / 1024.0);

                InputStream inputStream = _socket.getInputStream();
                OutputStream outputStream = _socket.getOutputStream();
                DataInputStream dataInput = new DataInputStream(new BufferedInputStream(inputStream, 65535));
                DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(outputStream, 65535));
                int index = 1;
                long start = System.currentTimeMillis();
                while (true) {
                    dataOutput.writeInt(message.getBytes().length);
                    dataOutput.write(message.getBytes());
                    dataOutput.flush();
                    int byteLength = dataInput.readInt();
                    if (byteLength == -1) {
                        break;
                    }
                    byte[] bytes = new byte[byteLength];
                    dataInput.readFully(bytes, 0, byteLength);

                    if ((index % 1000) == 0) {
                        break;
                    }
                    index++;
                }
                System.out.println("server: " + index + " messages with size " + messageSize + "kb sent with speed "
                        + index / ((System.currentTimeMillis() - start) / 1000.0) + " m/s");
                count++;
            }
        } catch (SocketException e) {
            System.out.println("normal client connection shutdown (s): " + e.getMessage());
        } catch (EOFException e) {
            System.out.println("normal client connection shutdown (eof): " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                _socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
