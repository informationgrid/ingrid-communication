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

package net.weta.components.communication.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import net.weta.components.communication.ICommunication;

/**
 * Service to instantiate proxy objects for one or more proxied objects on one or more remote proxy-server's.
 * 
 * <p/>created on 25.04.2006
 * 
 * @version $Revision$
 * @author jz
 * @author $Author${lastedit}
 * 
 */
public class ProxyService {

    /**
     */
    private ProxyService() {
        //
    }

    /**
     * @param communication
     * @param theInterface
     * @param proxyServerUrl
     * @return a proxy object from a proxy-server at the given url which implements the given interface
     */
    public static Object createProxy(ICommunication communication, Class theInterface, String proxyServerUrl) {
        return createProxy(communication, new Class[] { theInterface }, proxyServerUrl);
    }

    /**
     * @param communication
     * @param theInterfaces
     * @param proxyServerUrl
     * @return a proxy object from a proxy-server at the given url which implements the given interfaces
     */
    public static Object createProxy(ICommunication communication, Class[] theInterfaces, String proxyServerUrl) {
        InvocationHandler handler = new ReflectInvocationHandler(communication, proxyServerUrl);
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), theInterfaces, handler);
    }

    /**
     * Shortcut for installing the reflect message handler on given communication and adding the given object to the
     * reflectu message handler.
     * 
     * @param communication
     * @param interfac
     * @param object
     * @return the installed message handler
     */
    public static ReflectMessageHandler createProxyServer(ICommunication communication, Class interfac, Object object) {
        ReflectMessageHandler messageHandler = new ReflectMessageHandler();
        messageHandler.addObjectToCall(interfac, object);
        communication.getMessageQueue().addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE, messageHandler);
        return messageHandler;
    }

}
