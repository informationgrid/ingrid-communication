###
# **************************************************-
# ingrid-communication
# ==================================================
# Copyright (C) 2014 - 2024 wemove digital solutions GmbH
# ==================================================
# Licensed under the EUPL, Version 1.2 or – as soon they will be
# approved by the European Commission - subsequent versions of the
# EUPL (the "Licence");
# 
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
# https://joinup.ec.europa.eu/software/page/eupl
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

PORT=8080

java -cp $CLASSPATH net.weta.components.test.TcpServer --port $PORT
