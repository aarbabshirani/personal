export WPC_HOME=$PWD

# Edit to point to the JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
	# On OSX 10.8
	#export JAVA_HOME="/usr/libexec/java_home -v 1.8"
	
	# On Linux
	export JAVA_HOME="/usr/java/latest"
	
	# On Ubuntu
	#export JAVA_HOME="/usr/lib/jvm/java-8-oracle"
fi

VMSIZE=2G
PERSIZE=48M

LIB=$WPC_HOME/lib
CLASSPATH=$WPC_HOME/conf
for f in $LIB/*.jar; do
  if [ -f $f ]; then
    CLASSPATH=${CLASSPATH}:$f
  fi
done

export CLASSPATH
MAIN_CLASS=com.walmart.ticket.service.main.Startup
PID_FILE=wpc.PID
WPC_CFG=$WPC_HOME/conf

JAVA_OPTS="-Duser.timezone=GMT $JAVA_OPTS"
JAVA_OPTS="-Xmx$VMSIZE -Xms$VMSIZE -XX:PermSize=$PERSIZE -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:MaxGCPauseMillis=20 -XX:GCTimeRatio=99 -Djava.net.preferIPv4Stack=true"

echo $JAVA_OPTS

$JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH $MAIN_CLASS 2>&1
echo $! > $PID_FILE
cat $PID_FILE