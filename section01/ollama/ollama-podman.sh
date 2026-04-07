#!/bin/bash
set -e

CONTAINER_NAME="ollama"
OLLAMA_MODEL="llama3.2:1b"
OLLAMA_PORT=11434

echo ">>> Checking if container '$CONTAINER_NAME' already exists..."
if podman container exists "$CONTAINER_NAME"; then
  echo ">>> Container '$CONTAINER_NAME' found. Starting if not running..."
  podman start "$CONTAINER_NAME" 2>/dev/null || true
else
  echo ">>> Creating and starting Ollama container..."
  podman run -d \
    --name "$CONTAINER_NAME" \
    -p "$OLLAMA_PORT:11434" \
    -v ollama:/root/.ollama \
    ollama/ollama
fi

echo ">>> Waiting for Ollama to be ready..."
until curl -s "http://localhost:$OLLAMA_PORT/api/tags" > /dev/null 2>&1; do
  sleep 1
done

echo ">>> Pulling model '$OLLAMA_MODEL'..."
podman exec "$CONTAINER_NAME" ollama pull "$OLLAMA_MODEL"

echo ""
echo ">>> Ollama is ready at http://localhost:$OLLAMA_PORT"
echo ">>> Model '$OLLAMA_MODEL' is available."
