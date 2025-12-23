#!/bin/bash

source /etc/profile

APP_NAME="JDataViewServer-*.jar"
APP_HOME="/opt/JDataView"

start() {
    cd $APP_HOME
    nohup java -Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$APP_HOME/heapdump.hprof -jar $APP_NAME > /dev/null 2>&1 &
    echo "$APP_NAME started with PID $!"
}

stop() {
    pid=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
    [ -n "$pid" ] && kill -9 $pid
    echo "$APP_NAME stopped"
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo "Usage: $0 {start|stop|restart}"
        exit 1
esac