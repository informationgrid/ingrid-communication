package net.weta.components.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import net.weta.components.communication.ICommunication;

import org.apache.log4j.Logger;

public class StartCommunication {

    private static final Logger LOG = Logger.getLogger(StartCommunication.class);

    public static ICommunication create(InputStream inputStream) throws IOException {
        TcpCommunication communication = new TcpCommunication();
        configureFromProperties(inputStream, communication);
        return communication;
    }

    private static void configureFromProperties(InputStream inputStream, TcpCommunication peerService)
            throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        Set set = properties.keySet();
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            int indexOfSeperator = key.lastIndexOf('-');
            if (indexOfSeperator < 0) {
                indexOfSeperator = key.length();
            }
            Method method = getMethod(key.substring(0, indexOfSeperator), peerService.getClass());
            Object argValue = getArgumentValue(method, properties.getProperty(key));
            if (LOG.isDebugEnabled()) {
                LOG.debug("call '" + method.getName() + "' with '" + argValue + "'");
            }
            try {
                method.invoke(peerService, new Object[] { argValue });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Object getArgumentValue(Method method, String property) {
        if (method.getParameterTypes()[0].equals(String.class)) {
            return property;
        } else if (method.getParameterTypes()[0].equals(boolean.class)) {
            return new Boolean(property);
        } else if (method.getParameterTypes()[0].equals(int.class)) {
            return new Integer(property);
        } else if (method.getParameterTypes()[0].equals(long.class)) {
            return new Long(property);
        } else {
            throw new IllegalArgumentException("Unknown argument type: " + method.getParameterTypes()[0]);
        }
    }

    private static Method getMethod(String configKey, Class class1) {
        int index = configKey.indexOf(".");
        configKey = index > -1 ? configKey.substring(0, index) : configKey;
        String methodPrefix = index > -1 ? "add" : "set";
        Method[] methods = class1.getMethods();
        String methodName = methodPrefix + configKey;
        for (int i = 0; i < methods.length; i++) {
            if (methodName.equals(methods[i].getName())) {
                return methods[i];
            }
        }
        throw new IllegalStateException("configuration key '" + configKey + "' has no belonging method '" + methodName
                + "' in " + class1.getName());
    }

}
