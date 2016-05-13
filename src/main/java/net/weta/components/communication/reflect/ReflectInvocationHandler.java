/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
import java.lang.reflect.Method;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.PayloadMessage;

/**
 * Intercept method call's on a prox object. Create a proxy object with:<br/>
 * 
 * ReflectInvocationHandler handler = new ReflectInvocationHandler(communicsation,"/group:proxyServerPeer");<br/>
 * AInterface server = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[] {
 * AInterface.class, AnotherInterface }, handler);
 * 
 * <p/> The key with which the proxied object must bound to the
 * {@link net.weta.components.communication.reflect.ReflectMessageHandler} on proxy.server side is the first interface
 * with which the proxy object is initiated.
 * 
 * <p/>
 * 
 * 
 * <p/>created on 25.04.2006
 * 
 * @version $Revision$
 * @author jz
 * @author $Author${lastedit}
 * 
 */
public class ReflectInvocationHandler implements InvocationHandler {

    private ICommunication fCommunication;

    private String fProxyServerUrl;

    /**
     * @param communication
     * @param proxyServerUrl
     */
    public ReflectInvocationHandler(ICommunication communication, String proxyServerUrl) {
        this.fCommunication = communication;
        this.fProxyServerUrl = proxyServerUrl;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String objectToCallClassName = proxy.getClass().getInterfaces()[0].getName();
        ReflectMessage reflectMessage = new ReflectMessage(method.getName(), objectToCallClassName, args);
        PayloadMessage reply = (PayloadMessage) this.fCommunication.sendSyncMessage(reflectMessage,
                this.fProxyServerUrl);
        if (reply.getPayload() instanceof Exception) {
            throw (Throwable) reply.getPayload();
        }

        return reply.getPayload();
    }

}
