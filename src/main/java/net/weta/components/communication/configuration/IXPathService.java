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
package net.weta.components.communication.configuration;

import java.io.File;
import java.io.InputStream;

public interface IXPathService {

    void registerDocument(File xmlFile) throws Exception;

    void registerDocument(InputStream xmlFile) throws Exception;

    String parseAttribute(String nodePath, String attributeName) throws Exception;

    String parseAttribute(String nodePath, String attributeName, int item) throws Exception;

    void setAttribute(String nodePath, String attributeName, String value, int item) throws Exception;

    boolean exsistsNode(String nodePath);

    boolean exsistsNode(String nodePath, int item);

    int countNodes(String nodePath) throws Exception;
    
    // new ones
    
    /**
     * setAttribute()
     * 
     * Sets the value of an existing attribute.
     * Would be the same like setAttribue(nodePath, attribueName, value, 0) if more than one node matches the path.
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @param   String  value           the value of the attribute
     * @throws  Exception   is thrown if node does not exist
     */
    void setAttribute(String nodePath, String attributeName, String value) throws Exception;
    
    /**
     * addAttribute()
     * 
     * Adds or overwrites an existing attribute of a node with given value.
     * If more than one nodes matches the path the first one is chosen.
     * Would be the same like addAttribue(nodePath, attribueName, value, 0);
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @param   String  value           the value of the attribute
     * @throws  Exception   is thrown if node does not exist
     */
    void addAttribute(String nodePath, String attributeName, String value) throws Exception;
    
    /**
     * addAttribute()
     * 
     * Adds or overwrites an existing attribute of a node with given value.
     * Useful if more than one of given node matches the path.
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @param   String  value           the value of the attribute
     * @param   int     item            the index of the node
     * @throws  Exception   is thrown if node does not exist
     */
    void addAttribute(String nodePath, String attributeName, String value, int item) throws Exception;
    
    /**
     * existsAttribute()
     * 
     * Checks if a attribute exists for a node.
     * If more than one nodes matches the path the first one is chosen.
     * Would be the same lie existsAttribute(nodePath, attributeName, 0);
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @return  boolean true, if node has given attribute, otherwise false
     */
    boolean existsAttribute(String nodePath, String attributeName);
    
    /**
     * existsAttribute()
     * 
     * Checks if a attribute exists for a node.
     * Useful if more than one of given node matches the path.
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @param   int     item            the index of the node
     * @return  boolean true, if node has given attribute, otherwise false
     */
    boolean existsAttribute(String nodePath, String attributeName, int item);
    
    /**
     * removeAttribute()
     * 
     * Removes a specified attribute from a node.
     * If more than one nodes matches the path the first one is chosen.
     * Would be the same like removeAttribue(nodePath, attributeName, 0);
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @throws  Exception   is thrown if node does not exist or don't even has the given attribute
     */
    void removeAttribute(String nodePath, String attributeName) throws Exception;
    
    /**
     * removeAttribute()
     * 
     * Removes a specified attribute from a node.
     * Useful if more than one of given node matches the path.
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @param   int     item            the index of the node
     * @throws  Exception   is thrown if node does not exist or don't even has the given attribute
     */
    void removeAttribute(String nodePath, String attritubeName, int item) throws Exception;
    
    /**
     * removeAttributes()
     * 
     * Removes a specified attribute from all nodes that matches the path.
     * 
     * @author  me
     * @param   String  nodePath        the path of the node that holds the attribute
     * @param   String  attributeName   the name of the attribute (key)
     * @throws  Exception   is thrown if node does not exist or one of the nodes don't even has the given attribute
     */
    void removeAttributes(String nodePath, String attritubeName) throws Exception;
    
    /**
     * addNode()
     * 
     * Appends a node to the documents first node that matches.
     * Would be the same like addNode(parentPath, elementName, 0).
     * 
     * @author  me
     * @param   String  parentPath  the path to the parent node
     * @param   String  elementName the name of the new node
     * @throws  Exception   is thrown if parent node does not exist
     */
    void addNode(String parentPath, String elementName) throws Exception;
    
    /**
     * addNode()
     * 
     * Appends a node to the documents specific node.
     * 
     * @author  me
     * @param   String  parentPath  the path to the parent node
     * @param   String  elementName the name of the new node
     * @param   int     the position of the node
     * @throws  Exception   is thrown if parent node does not exist
     */
    void addNode(String parentPath, String elementName, int item) throws Exception;
    
    /**
     * removeNode()
     * 
     * Removes a node from the document.
     * If more nodes of this kind exists the first one gets deleted.
     * Would be the same like removeNode(nodePath, 0).
     * 
     * @author  me
     * @param   String  nodePath    the path to the node that should be removed
     * @throws  Exception   is thrown if node does not exist
     */
    void removeNode(String nodePath) throws Exception;
    
    /**
     * removeNode()
     * 
     * Removes a node from the document by its index.
     * Useful if more than one of this kind exists.
     * 
     * @author  me
     * @param   String  nodePath    the path to the node that should be removed
     * @param   int     index       the index
     * @throws  Exception   is thrown if index or node does not exist
     */
    void removeNode(String nodePath, int index) throws Exception;
    
    /**
     * removeNodes()
     * 
     * Remove all node of a kind from a document.
     *
     * @author  me
     * @param   String  nodePath    the path to the nodes that should be removed 
     * @throws  Exception
     */
    void removeNodes(String nodePath) throws Exception;
    
    // end: new ones

    void store(File xmlFile) throws Exception;

}
