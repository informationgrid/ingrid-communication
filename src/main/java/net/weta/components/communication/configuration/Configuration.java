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
package net.weta.components.communication.configuration;

public abstract class Configuration {

    private String _name;
    private int _queueSize = 2000;
    private int _handleTimeout = 10;

    public String getName() {
        return _name;
    }

    public void setName(String serverName) {
        _name = serverName;
    }

    public void setQueueSize(int queueSize) {
        _queueSize = queueSize;
    }

    public void setHandleTimeout(int handleTimeout) {
        _handleTimeout = handleTimeout;
    }

    public int getQueueSize() {
        return _queueSize;
    }

    public int getHandleTimeout() {
        return _handleTimeout;
    }

}
