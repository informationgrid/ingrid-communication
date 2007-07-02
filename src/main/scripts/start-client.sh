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
