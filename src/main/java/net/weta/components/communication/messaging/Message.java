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
 * This class can be use for sending object (as byte[]) over the comnunication component.
 * 
 * @version $Revision$
 */
public class Message implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2185856429974224747L;

    private String _type = "";

    private int _id;

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
    public int getId() {
        return _id;
    }

    /**
     * @param id
     */
    public void setId(int id) {
        _id = id;
    }

}
