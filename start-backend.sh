#!/usr/bin/env bash

# Navigate to monorepo root
ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$ROOT_DIR"

# Load environment variables from .env if present
if [ -f .env ]; then
  echo "🚀 Loading environment variables from .env..."
  export $(grep -v '^#' .env | xargs)
else
  echo "⚠️ Warning: .env file not found at root!"
fi

# Kill any existing process running on port 8080 to prevent port conflict
fuser -k 8080/tcp 2>/dev/null || true

echo "🟢 Starting Spring Boot Backend..."
cd backend/chatPOC
./mvnw spring-boot:run
