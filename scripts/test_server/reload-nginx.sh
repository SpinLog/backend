#!/bin/bash

CURRENT_PORT=$(cat /home/ubuntu/zips/path/service_url.inc | grep -Po '[0-9]+' | tail -1)
NEXT_PORT=0

if [ $CURRENT_PORT -eq 8082 ]; then
  NEXT_PORT=8084
elif [ $CURRENT_PORT -eq 8084 ]; then
  NEXT_PORT=8082
else
  echo "Invalid port number. Exiting script."
  exit 1
fi

CURRENT_MONITORING_PORT=$(($CURRENT_PORT + 1))
NEXT_MONITORING_PORT=$(($NEXT_PORT + 1))

echo "set \$service_url http://127.0.0.1:${NEXT_PORT};" | tee /home/ubuntu/zips/path/service_url.inc
echo "set \$monitoring_url http://127.0.0.1:${NEXT_MONITORING_PORT};" | tee /home/ubuntu/zips/path/monitoring_url.inc

echo "> Now Service port is ${NEXT_PORT}."
echo "> Now Monitoring port is ${NEXT_MONITORING_PORT}."

sudo systemctl reload nginx

echo "> Nginx reloaded."
echo ""

# health check

response_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)

if [ $response_code -ne 200 ]; then
  echo "> Health check failed."
  exit 1
fi

echo "> nginx health check succeeded"

# kill previous server
echo "> kill previous server"

CURRENT_PID=$(lsof -ti tcp:${CURRENT_PORT})

if [ -z "$CURRENT_PID" ]; then
  echo "> There is no running server."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
fi