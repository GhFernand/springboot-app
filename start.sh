#!/bin/bash
nohup java -jar target/integration-0.0.1-SNAPSHOT.jar > /tmp/middleware.log 2>&1 &
