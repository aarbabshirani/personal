#!/bin/sh

PID_FILE="wpc.PID"
if [ ! -f $PID_FILE ];
then
   echo "File $PID_FILE does not exists"
   exit 0
fi

KILL_WAIT_TIME=5
read pid < $PID_FILE
echo kill $pid
kill $pid
sleep $KILL_WAIT_TIME
if  ps -p $pid >&- ; then
  echo 'process not terminated.  force kill now...'
  kill -9 $pid >/dev/null 2>&1
else
  echo 'process terminated'
fi
rm $PID_FILE