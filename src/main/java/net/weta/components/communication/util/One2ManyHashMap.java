/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

package net.weta.components.communication.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An unsynchronized map like structure which maintains for each key a {@link java.util.Set} of values.
 * 
 * <p/>created on 22.10.2005
 * 
 * @version $Revision$
 * @author jz
 * @author $Author${lastedit}
 * 
 */
public class One2ManyHashMap {

    protected HashMap fHashMap = new HashMap();

    /**
     * @param key
     * @param value
     * @return if value could be added to set
     */
    public boolean add(Object key, Object value) {
        Set set = getValues(key);
        if (set == null) {
            set = new HashSet(3);
        }
        boolean success = set.add(value);
        this.fHashMap.put(key, set);

        return success;
    }

    /**
     * @param key
     * @return the set to the given key or null if none exists
     */
    public Set getValues(Object key) {
        return (Set) this.fHashMap.get(key);
    }

    /**
     * Removes the value from the given key set.
     * 
     * @param key
     * @param value
     * @return if value has exists or not
     */
    public boolean removeValue(Object key, Object value) {
        Set set = getValues(key);
        boolean success = set.remove(value);
        if (set.size() == 0) {
            this.fHashMap.remove(key);
        }

        return success;
    }

    /**
     * @param key
     * @return a set with all values belonging to the given key
     */
    public Set removeKey(Object key) {
        Set set = getValues(key);
        this.fHashMap.remove(key);

        return set;
    }

    /**
     * @see HashMap#keySet()
     * @return a set view of the keys contained in this map.
     */
    public Set keySet() {
        return this.fHashMap.keySet();
    }

    /**
     * @return all values in a set
     */
    public Set valueSet() {
        Set allValues = new HashSet(size() + 10);
        for (Iterator iter = keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            allValues.addAll(getValues(key));
        }

        return allValues;
    }

    /**
     * @see HashMap#size()
     * @return the size of the map
     */
    public int size() {
        return this.fHashMap.size();
    }
}
