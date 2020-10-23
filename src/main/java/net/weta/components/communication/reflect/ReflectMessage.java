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

import java.io.IOException;

import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.stream.IInput;
import net.weta.components.communication.stream.IOutput;

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

    private static final int MAX_DEEP = 50;

    private String _objectToCallClass = "";

    private String _methodName = "";

    private Object[] _arguments = new Object[0];

    public ReflectMessage() {
    }

    /**
     * @param methodName
     * @param objectToCallClass
     */
    public ReflectMessage(String methodName, String objectToCallClass) {
        this(methodName, objectToCallClass, new Object[0]);
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
        _arguments = arguments != null ? arguments : new Object[0];
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

    public void read(IInput in) throws IOException {
        _objectToCallClass = in.readString();
        _methodName = in.readString();
        _arguments = (Object[]) in.readObject();
        super.read(in);
    }

    public void write(IOutput out) throws IOException {
        out.writeString(_objectToCallClass);
        out.writeString(_methodName);
        out.writeObject(_arguments);
        super.write(out);
    }

    @Override
    public String toString() {
        String ret = _objectToCallClass + "_" + _methodName;
        ret += deepString(_arguments, 1);
        return ret;
    }

    private String deepString(Object object, int deep) {
        String ret = "";
        if (deep < MAX_DEEP) {
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                for (Object object2 : objects) {
                    ret += deepString(object2, deep + 1);
                }
            } else {
                ret += "_" + ((object == null) ? "" : object.toString());
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        ReflectMessage reflectMessage = new ReflectMessage("foo", "bar", new String[] { "1", "2", "3" });
        System.out.println(reflectMessage.toString() + " " + reflectMessage.hashCode());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = computeHash(_methodName, prime, 1, 50);
        hash = prime * hash + (_objectToCallClass == null ? 0 : _objectToCallClass.hashCode());
        return prime * _methodName.hashCode() + computeHash(_arguments, prime, 1, 50);
    }
    
    private int computeHash(final Object obj, final int prime, final int deep, final int maxDeep) {
        int hash = 0;
        if (obj instanceof Object[] && deep < maxDeep) {
            Object[] objs = (Object[]) obj;
            for (Object obj2 : objs) {
                hash = prime * hash + computeHash(obj2, prime, deep + 1, maxDeep);
            }
        } else {
            hash = prime * hash + (obj == null ? 0 : obj.hashCode());
        }
        return hash;
    }
}
