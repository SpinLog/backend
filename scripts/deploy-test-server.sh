#!/usr/bin/bash

source ~/zips/mysql_secret.sh

TARGET_PORT=8082

export monitoring_port=$(($TARGET_PORT + 1))

echo "start updating WAS version"

nohup java -Xms128m -Xmx512m \
        -XX:+HeapDumpOnOutOfMemoryError \
        --add-opens java.base/java.lang=ALL-UNNAMED \
        -javaagent:${SCOUTER_AGENT_DIR}/scouter.agent.jar \
        -Dscouter.config=${SCOUTER_AGENT_DIR}/conf/scouter.conf \
        -Dserver.port=${TARGET_PORT} \
        -Duser.timezone=Asia/Seoul \
        -Dspring.profiles.active=dev \
        -jar build/libs/spinlog-0.0.1-SNAPSHOT.jar \
        >> /home/ubuntu/logs/was_out.log 2> /home/ubuntu/logs/was_err.log < /dev/null &

PID=$!

echo $PID | sudo tee /sys/fs/cgroup/example/tasks/cgroup.procs

echo "Program is running with PID $PID and has been added to cgroup 'tasks'"
