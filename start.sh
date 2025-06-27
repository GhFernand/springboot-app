#!/bin/bash
echo "[start.sh] Rodando script no diretório: $(pwd)"
echo "[start.sh] Iniciando aplicação..."
nohup java -jar target/integration-0.0.1-SNAPSHOT.jar > /tmp/middleware.log 2>&1 &
