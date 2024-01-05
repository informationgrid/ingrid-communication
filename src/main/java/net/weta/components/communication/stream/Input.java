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
package net.weta.components.communication.stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Input implements IInput {

    private final DataInput _dataInput;

    private final int _maxMessageSize;

    private static final Logger LOG = LogManager.getLogger(Input.class);
    
    public Input(DataInput dataInput, int maxMessageSize) {
        _dataInput = dataInput;
        _maxMessageSize = maxMessageSize;
    }

    public byte[] readBytes() throws MessageSizeTooBigException, IOException {
        int length = readInt();
        if (length > _maxMessageSize) {
            byte[] bytes = new byte[256];
            try {
                _dataInput.readFully(bytes);
            } catch (Exception e) {}
            throw new MessageSizeTooBigException("message size too big: [" + length + "]. Message starts with: " + new String(bytes, "UTF-8"));
        }
        byte[] bytes = new byte[length];
        _dataInput.readFully(bytes);
        return bytes;
    }

    public int readInt() throws IOException {
        return _dataInput.readInt();
    }

    public Object readObject() throws MessageSizeTooBigException, IOException {
        byte[] bytes = readBytes();
        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object object;
        try {
            object = stream.readObject();
        } catch (ClassNotFoundException e) {
            LOG.error("class not found: " + e.getMessage(), e);
            throw new IOException("class not found: " + e.getMessage());
        } finally {
            stream.close();
        }
        return object;
    }

    public String readString() throws MessageSizeTooBigException, IOException {
        byte[] bytes = readBytes();
        return new String(bytes);
    }

    public boolean readBoolean() throws IOException {
        return _dataInput.readBoolean();
    }

}
