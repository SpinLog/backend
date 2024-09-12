#!/usr/bin/bash

source ~/zips/mysql_secret.sh

CURRENT_PORT=$(cat /home/ubuntu/zips/path/service_url.inc | grep -Po '[0-9]+' | tail -1)

TARGET_PORT=0

if [ $CURRENT_PORT -eq 8082 ]; then
  TARGET_PORT=8084
elif [ $CURRENT_PORT -eq 8084 ]; then
  TARGET_PORT=8082
else
  echo "Invalid port number. Exiting script."
  exit 1
fi

export monitoring_port=$(($TARGET_PORT + 1))

echo "start updating WAS version"

nohup java -Xms128m -Xmx512m \
        -XX:+HeapDumpOnOutOfMemoryError \
        -jar -Dserver.port=${TARGET_PORT} -Dspring.profiles.active=dev build/libs/spinlog-0.0.1-SNAPSHOT.jar \
        >> ../logs/was_out.log 2> ../logs/was_err.log < /dev/null &

pid=$!

echo $pid | sudo tee /sys/fs/cgroup/example/tasks/cgroup.procs

echo "Program is running with PID $pid and has been added to cgroup 'tasks'"