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

import net.weta.components.communication.messaging.Message;

/**
 * TODO comment for ReflectMessage
 * 
 * <p/>created on 25.04.2006
 * 
 * @version $Revision$
 * @author jz
 * @author $Author${lastedit}
 * 
 */
public class ReflectMessage extends Message {

    /***/
    private static final long serialVersionUID = 1322251878305777946L;

    private String _objectToCallClass;

    private String _methodName;

    private Object[] _arguments;

    /**
     * @param methodName
     * @param objectToCallClass
     */
    public ReflectMessage(String methodName, String objectToCallClass) {
        this(methodName, objectToCallClass, null);
    }

    /**
     * @param methodName
     * @param objectToCallClass
     * @param arguments
     */
    public ReflectMessage(String methodName, String objectToCallClass, Object[] arguments) {
        super(ReflectMessageHandler.MESSAGE_TYPE);
        _methodName = methodName;
        _objectToCallClass = objectToCallClass;
        _arguments = arguments;
    }

    /**
     * @return the name of the method to invoke
     */
    public String getMethodName() {
        return _methodName;
    }

    /**
     * @return the name of the method that delivers the object to call
     */
    public String getObjectToCallClass() {
        return _objectToCallClass;
    }

    /**
     * @return the argument classes or null
     */
    public Class[] getArgumentClasses() {
        if (_arguments == null) {
            return null;
        }

        Class[] classes = new Class[_arguments.length];
        for (int i = 0; i < _arguments.length; i++) {
            if (_arguments[i] instanceof Integer) {
                classes[i] = int.class;
            } else if (_arguments[i] != null) {
                classes[i] = _arguments[i].getClass();
            }
        }
        return classes;
    }

    /**
     * @return the arguments or null
     */
    public Object[] getArguments() {
        return _arguments;
    }
}