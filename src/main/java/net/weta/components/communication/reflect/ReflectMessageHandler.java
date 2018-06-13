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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.weta.components.communication.CommunicationException;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.messaging.PayloadMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO comment for ReflectMessageHandler
 * 
 * <p/>created on 25.04.2006
 * 
 * @version $Revision$
 * @author jz
 * @author $Author${lastedit}
 * 
 */
public class ReflectMessageHandler implements IMessageHandler {

    private static Logger _LOGGER = LogManager.getLogger(ReflectMessageHandler.class);

    /***/
    public static final String MESSAGE_TYPE = ReflectMessageHandler.class.getName();

    private HashMap _ObjectsToCallByClassName = new HashMap(3);

    public ReflectMessageHandler() {
        // nothing todo
    }

    /**
     * @param interfac ,
     *            the key a proxy service uses for accessing the proxied object.
     * @param object
     */
    public void addObjectToCall(Class interfac, Object object) {
        String key = interfac.getName();
        if (_ObjectsToCallByClassName.containsKey(key)) {
            throw new IllegalArgumentException("object of same class already contained");
        }
        _ObjectsToCallByClassName.put(key, object);
    }

    public Message handleMessage(Message message) {
        ReflectMessage reflectMessage = (ReflectMessage) message;
        if (_LOGGER.isDebugEnabled()) {
            _LOGGER.debug("handle message [" + message.getId() + "]");
        }
        Serializable reply;
        try {
            Object object = getObjectToCall(reflectMessage);
            Method method = getMethod(object, reflectMessage);
            if (_LOGGER.isDebugEnabled()) {
                _LOGGER.debug("Invoke [" + object + "].[" + method + "] with arguments [" + reflectMessage.getArguments() + "]");
            }
            reply = (Serializable) method.invoke(object, reflectMessage.getArguments());
        } catch (Throwable e) {
            if (_LOGGER.isErrorEnabled()) {
                _LOGGER.error("local exception on proxy-method-call '" + reflectMessage.getMethodName() + "' on object '" + reflectMessage.getObjectToCallClass() + "' for message-id " + "["
                        + message.getId() + "]", e);
            }
            reply = new CommunicationException(getStackTraceAsString(e));
        }
        PayloadMessage payloadMessage = new PayloadMessage(reply, message.getType());
        payloadMessage.setId(message.getId());
        return payloadMessage;
    }

    private Method getMethod(Object object, ReflectMessage reflectMessage) throws NoSuchMethodException {
        Method method;
        try {
            method = object.getClass().getMethod(reflectMessage.getMethodName(), reflectMessage.getArgumentClasses());
        } catch (Exception e) {
            Method[] methods = object.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(reflectMessage.getMethodName())
                        && argumentsAssignable(methods[i].getParameterTypes(), reflectMessage.getArgumentClasses())) {
                    return methods[i];
                }
            }
            throw new NoSuchMethodException(reflectMessage.getMethodName());
        }
        return method;
    }

    private boolean argumentsAssignable(Class[] parameterTypes, Class[] argumentClasses) {
        if (parameterTypes.length != argumentClasses.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (argumentClasses[i] != null && !parameterTypes[i].isAssignableFrom(argumentClasses[i])) {
                return false;
            }
        }
        return true;
    }

    private Object getObjectToCall(ReflectMessage reflectMessage) throws IllegalArgumentException {
        Object object = _ObjectsToCallByClassName.get(reflectMessage.getObjectToCallClass());
        if (object == null) {
            throw new IllegalArgumentException("object to call of type '" + reflectMessage.getObjectToCallClass()
                    + "' not installed");
        }
        return object;
    }
    
    private String getStackTraceAsString(final Throwable exception) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(baos));
        return new String(baos.toByteArray());
    }

}
