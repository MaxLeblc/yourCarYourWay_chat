#!/usr/bin/env bash

# Navigate to monorepo root
ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$ROOT_DIR"

echo "🎨 Starting Angular Frontend (chatUI)..."
cd frontend/chatUI
npm start
