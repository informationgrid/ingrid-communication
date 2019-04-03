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
