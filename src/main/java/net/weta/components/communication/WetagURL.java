/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package net.weta.components.communication;

import java.io.Serializable;

public class WetagURL implements Serializable {

    private static final long serialVersionUID = 4405720578061664363L;

    public static final String PROTOCOL = "wetag";

    private String _groupPath = "";

    private String _peerName = "";

    private String _path = "";

    public WetagURL(String url) throws IllegalArgumentException {
        if (url == null) {
            throw new NullPointerException("url could not be null");
        }
        if (!url.startsWith(PROTOCOL)) {
            url = PROTOCOL + "://" + url;
        }
        parse(url);
        normalizePath();
        this._path = constructPath();
    }

    private String constructPath() {
        StringBuffer buffer = new StringBuffer(this._groupPath);
        if (this._peerName.length() > 0) {
            buffer.append(':');
            buffer.append(this._peerName);
        }
        return buffer.toString();
    }

    private void parse(String url) throws IllegalArgumentException {
        if (!url.startsWith(PROTOCOL + "://")) {
            throw new IllegalArgumentException("URL has wrong protocoll: " + url);
        }

        if (url.matches(PROTOCOL + "://.*?:.*?")) {
            String path = url.replaceFirst(PROTOCOL + "://(.*)?:(.*)?", "$1");
            path = path.trim();
            if ((path != null) && (path.length() > 0) && path.startsWith("/")) {
                this._groupPath = path;
            } else {
                throw new IllegalArgumentException("URL has wrong path: " + url);
            }

            String receiver = url.replaceFirst(PROTOCOL + "://(.*)?:(.*)?", "$2");
            receiver.trim();
            if ((receiver != null) && (receiver.length() > 0)) {
                this._peerName = receiver;
            }
        } else {
            String path = url.replaceFirst(PROTOCOL + "://(.*)?", "$1");
            path.trim();
            if ((path != null) && (path.length() > 0) && path.startsWith("/")) {
                this._groupPath = path;
            } else {
                throw new IllegalArgumentException("URL has wrong path: " + url);
            }
        }
    }

    private void normalizePath() {
        String path = this._groupPath.replaceFirst("(?m)^[/]*(.*?)", "$1");
        path = path.replaceFirst("(?m)(.*?)[/]*?$", "$1");
        this._groupPath = "/" + path;
    }

    public String getURL() {
        StringBuffer buffer = new StringBuffer(PROTOCOL);
        buffer.append("://");
        buffer.append(this._groupPath);
        if (this._peerName.length() > 0) {
            buffer.append(':');
            buffer.append(this._peerName);
        }

        return buffer.toString();
    }

    public String getPeerName() {
        return this._peerName;
    }

    public String getGroupPath() {
        return this._groupPath;
    }

    public boolean hasPeerName() {
        return (this._peerName.length() > 0) ? true : false;
    }

    public String getPath() {
        return this._path;
    }

    public String toString() {
        return getURL();
    }

    public static WetagURL createUrl(String group, String peerName) {
        StringBuffer buffer = new StringBuffer(PROTOCOL);
        buffer.append("://");
        buffer.append(group);
        buffer.append(':');
        buffer.append(peerName);
        return new WetagURL(buffer.toString());
    }

}
