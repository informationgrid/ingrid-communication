/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package net.weta.components.communication.messaging;

import java.io.IOException;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

public class RegistrationMessage extends Message {

    private static final long serialVersionUID = 4579214339793912508L;

    private String _registrationName = "";

    private byte[] _signature = new byte[0];

    public String getRegistrationName() {
        return _registrationName;
    }

    public void setRegistrationName(String registrationName) {
        _registrationName = registrationName;
    }

    public byte[] getSignature() {
        return _signature;
    }

    public void setSignature(byte[] signature) {
        _signature = signature;
    }

    public void read(IInput in) throws IOException {
        _registrationName = in.readString();
        _signature = in.readBytes();
        super.read(in);
    }

    public void write(IOutput out) throws IOException {
        out.writeString(_registrationName);
        out.writeBytes(_signature);
        super.write(out);
    }

}
