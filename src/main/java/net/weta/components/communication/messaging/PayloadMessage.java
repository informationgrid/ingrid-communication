/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
import java.io.Serializable;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

public class PayloadMessage extends Message {

    private static final long serialVersionUID = 2685915429239362958L;

    private Serializable _payload;

    public PayloadMessage() {
    }

    public PayloadMessage(Serializable payload, String type) {
        super(type);
        _payload = payload;
    }

    public Serializable getPayload() {
        return _payload;
    }

    public void read(IInput in) throws IOException {
        _payload = (Serializable) in.readObject();
        super.read(in);
    }

    public void write(IOutput out) throws IOException {
        out.writeObject(_payload);
        super.write(out);
    }
}
