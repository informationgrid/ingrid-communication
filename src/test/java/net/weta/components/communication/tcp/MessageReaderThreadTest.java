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
package net.weta.components.communication.tcp;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.TestCase;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.MessageQueue;
import net.weta.components.communication.messaging.PayloadMessage;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.stream.Input;
import net.weta.components.communication.stream.Output;
import net.weta.components.test.DummyExternalizable;

public class MessageReaderThreadTest extends TestCase {

    Thread _thread;

    boolean _isStarted = false;

    MessageReaderThread _mrThread;

    private ServerSocket _serverSocket;

    protected void setUp() throws Exception {
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    _serverSocket = new ServerSocket(65535);
                } catch (IOException e) {
                    fail(e.getMessage());
                }
                synchronized (_thread) {
                    _thread.notify();
                    _isStarted = true;
                }
                Socket isocket = null;
                IInput input = null;
                try {
                    isocket = _serverSocket.accept();
                    input = new Input(new DataInputStream(isocket.getInputStream()), 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                }
                _mrThread = new MessageReaderThread("name", input, new MessageQueue(), null, 1024 * 1204);
                _mrThread.start();
            }
        };
        _thread = new Thread(runnable);
        _thread.start();
    }

    public void testEOFException() throws InterruptedException {
        Socket socket = null;
        try {
            synchronized (_thread) {
                if (!_isStarted) {
                    _thread.wait();
                }
            }
            socket = new Socket("localhost", 65535);
            IOutput out = new Output(new DataOutputStream(socket.getOutputStream()));
            new Message().write(out);
        } catch (EOFException e) {
            assertTrue(true);
        } catch (IOException e) {
            fail();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        try {
            _serverSocket.close();
        } catch (IOException e) {
            // nothing todo
        }
    }

    public void testMaxThreadCount() {
        Socket socket = null;
        
        try {
            synchronized (_thread) {
                if (!_isStarted) {
                    _thread.wait();
                }
            }
            socket = new Socket("localhost", 65535);
            IOutput os = new Output(new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));
            PayloadMessage pmsg = new PayloadMessage(new DummyExternalizable(), "bla");
            pmsg.setId("1");
            pmsg.write(os);
            pmsg.setId("2");
            pmsg.write(os);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        try {
            if (_thread != null) {
                _thread.join();
            }
            _serverSocket.close();
        } catch (Exception e) {
            // nothing todo
        }
    }
}
