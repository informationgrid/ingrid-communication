/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package net.weta.components.communication.util;

import java.util.Set;

import junit.framework.TestCase;

public class One2ManyHashMapTest extends TestCase {

    One2ManyHashMap _one2ManyHashMap;

    protected void setUp() {
        _one2ManyHashMap = new One2ManyHashMap();
    }

    public void testRemoveValue() {
        Object key = new String("key1");
        Object value1 = new String("value1");
        Object value2 = new String("value2");
        _one2ManyHashMap.add(key, value1);
        _one2ManyHashMap.add(key, value2);
        Set values = _one2ManyHashMap.getValues(key);
        assertEquals(2, values.size());
        assertTrue(values.contains(value1));
        assertTrue(values.contains(value2));
        _one2ManyHashMap.removeValue(key, value1);
        assertEquals(1, values.size());
        assertTrue(values.contains(value2));

        _one2ManyHashMap.removeValue(key, value2);
        assertNull(_one2ManyHashMap.getValues(key));
    }

    public void testRemoveKey() {
        Object key = new String("key1");
        Object value1 = new String("value1");
        Object value2 = new String("value2");
        _one2ManyHashMap.add(key, value1);
        _one2ManyHashMap.add(key, value2);
        Set values = _one2ManyHashMap.getValues(key);
        assertEquals(2, values.size());
        assertTrue(values.contains(value1));
        assertTrue(values.contains(value2));
        _one2ManyHashMap.removeKey(key);
        assertNull(_one2ManyHashMap.getValues(key));
    }

}
