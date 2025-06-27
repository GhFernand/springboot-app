#!/bin/bash
echo "[start.sh] Rodando script no diretório: $(pwd)" >> /tmp/middleware.log
echo "[start.sh] Iniciando aplicação..." >> /tmp/middleware.log
nohup java -jar target/integration-0.0.1-SNAPSHOT.jar >> /tmp/middleware.log 2>&1 &
