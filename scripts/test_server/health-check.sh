#!/bin/bash

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

max_attempts=30
success=0
MONITORING_PORT=$(($TARGET_PORT + 1))

for ((counter=1; counter<=max_attempts; counter++))
do
  RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$MONITORING_PORT/actuator/health)

  if [ $RESPONSE_CODE -eq 200 ]; then
    echo "Request successful with response code 200. Exiting loop."
    success=1
    break
  else
    echo "Request failed with response code $RESPONSE_CODE. Retrying... ($counter/$max_attempts)"
  fi

  sleep 1
done

if [ $success -eq 0 ]; then
  echo "Request failed $max_attempts times. Exiting script."
  exit 1
fi

