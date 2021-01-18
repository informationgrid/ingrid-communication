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
/*
 * Copyright 2004-2005 weta group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  $Source$
 */

package net.weta.components.communication.messaging;

import java.io.IOException;
import java.io.Serializable;

import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;
import net.weta.components.communication.stream.IStreamable;

/**
 * This class can be use for sending object (as byte[]) over the comnunication
 * component.
 * 
 * @version $Revision$
 */
public class Message implements IStreamable, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2185856429974224747L;

    private String _type = "";

    private String _id = "";

    public Message() {
    }

    /**
     * @param type
     */
    public Message(String type) {
        _type = type;
    }

    /**
     * @return the type for this message
     */
    public String getType() {
        return _type;
    }

    /**
     * @param type
     */
    public void setType(String type) {
        _type = type;
    }

    /**
     * @return the id of the message if it is a responsfull message
     */
    public String getId() {
        return _id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        _id = id;
    }

    public void read(IInput input) throws IOException {
        _id = input.readString();
        _type = input.readString();
    }

    public void write(IOutput output) throws IOException {
        output.writeString(_id);
        output.writeString(_type);
        output.flush();
    }

}
