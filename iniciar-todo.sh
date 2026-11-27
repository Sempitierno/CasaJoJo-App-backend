#!/usr/bin/env bash
# Compila y levanta los 4 servicios (Mac/Linux). Ctrl+C detiene todo.
set -e
cd "$(dirname "$0")"
./mvnw -q -DskipTests install
trap 'kill 0' EXIT
for m in ms-usuarios ms-productos ms-pedidos gateway; do
  (cd "$m" && java -jar "target/$m-1.0.0.jar") &
  sleep 2
done
echo "Gateway en http://localhost:8080  (Ctrl+C para detener)"
wait
