###
# **************************************************-
# ingrid-communication
# ==================================================
# Copyright (C) 2014 - 2022 wemove digital solutions GmbH
# ==================================================
# Licensed under the EUPL, Version 1.1 or – as soon they will be
# approved by the European Commission - subsequent versions of the
# EUPL (the "Licence");
# 
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
# http://ec.europa.eu/idabc/eupl5
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and
# limitations under the Licence.
# **************************************************#
###
# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`

CLASSPATH=${INGRID_CONF_DIR:=$INGRID_HOME/conf}

for f in $INGRID_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

HOST=127.0.0.1
PORT=8080
PROXY_HOST=127.0.0.1
PROXY_PORT=8080
MAX_MESSAGES=10
USER_NAME=
PASSWORD=

if [ $1 -eq 0 ]; then
  echo 'run client without proxy.'
  java -cp $CLASSPATH net.weta.components.test.TcpClient --host $HOST --port $PORT --maxMessages $MAX_MESSAGES
elif [ $1 -eq 1 ]; then
 echo 'run client with proxy.'
 java -cp $CLASSPATH net.weta.components.test.TcpClient --host $HOST --port $PORT --proxyHost $PROXY_HOST --proxyPort $PROXY_PORT --maxMessages $MAX_MESSAGES --userName $USER_NAME --password $PASSWORD
fi
