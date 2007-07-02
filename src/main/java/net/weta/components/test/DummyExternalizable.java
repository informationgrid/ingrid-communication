package net.weta.components.test;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class DummyExternalizable extends HashMap implements Externalizable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DummyExternalizable() {
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        int numberOfKeys = keySet().size();
        out.writeInt(numberOfKeys);
        Iterator iterator = keySet().iterator();
        while (iterator.hasNext()) {
            Serializable key = (Serializable) iterator.next();
            out.writeObject(key);
            Serializable value = (Serializable) get(key);

            out.writeObject(value);
        }
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int numberOfKeys = in.readInt();
        for (int i = 0; i < numberOfKeys; i++) {
            Serializable key = (Serializable) in.readObject();
            Serializable value = (Serializable) in.readObject();

            put(key, value);
        }
    }

}
