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

import org.apache.log4j.Logger;

/**
 * Message handler for testing. Prints out the received message.
 * 
 * created on 28.12.2004
 * 
 * @version $Revision$
 */
public class TestMessageProcessor implements IMessageHandler {

    private static Logger LOGGER = Logger.getLogger(TestMessageProcessor.class);

    public Message handleMessage(Message message) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("handle message: " + message.toString());
        }
        if (message.getId() == -1) {
            throw new RuntimeException();
        }
        return message;
    }
}
