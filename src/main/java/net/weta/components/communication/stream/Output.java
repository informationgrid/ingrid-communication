/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package net.weta.components.communication.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Output implements IOutput {

    private final DataOutputStream _dataOutput;

    public Output(DataOutputStream dataOutput) {
        _dataOutput = dataOutput;
    }

    public void writeInt(int i) throws IOException {
        _dataOutput.writeInt(i);
    }

    public void writeObject(Object object) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(arrayOutputStream);
        stream.writeObject(object);
        byte[] bytes = arrayOutputStream.toByteArray();
        writeBytes(bytes);
        stream.close();
    }

    public void writeString(String string) throws IOException {
        writeBytes(string.getBytes());
    }

    public void writeBytes(byte[] bytes) throws IOException {
        writeInt(bytes.length);
        _dataOutput.write(bytes);
    }

    public void writeBoolean(boolean b) throws IOException {
        _dataOutput.writeBoolean(b);
    }

    public void flush() throws IOException {
        _dataOutput.flush();
    }
}
