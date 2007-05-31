# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`

CLASSPATH=${INGRID_CONF_DIR:=$INGRID_HOME/conf}

for f in $INGRID_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

PORT=8080

java -cp $CLASSPATH net.weta.components.test.TcpServer --port $PORT
