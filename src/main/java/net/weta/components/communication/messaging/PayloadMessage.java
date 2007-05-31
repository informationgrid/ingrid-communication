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

import java.io.Serializable;

/**
 * A message with a serializable payload.
 * 
 * <p/>created on 24.04.2006
 * 
 * @version $Revision$
 * @author jz
 * @author $Author${lastedit}
 * 
 */
public class PayloadMessage extends Message {

    private Serializable _payload;

    /**
     * @param type
     * @param payload
     */
    public PayloadMessage(Serializable payload, String type) {
        super(type);
        _payload = payload;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2685915429239362958L;

    /**
     * @return the payload
     */
    public Serializable getPayload() {
        return _payload;
    }

    /**
     * @param payload
     */
    public void setPayload(Serializable payload) {
        _payload = payload;
    }

}
