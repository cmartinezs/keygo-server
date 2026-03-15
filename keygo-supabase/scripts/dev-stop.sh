#!/bin/bash
# =========================================================
# Stop local development database
# Detener base de datos local de desarrollo
# =========================================================

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

echo "🛑 Stopping local development database / Deteniendo base de datos local de desarrollo..."

cd "$PROJECT_DIR"

# Stop docker compose
docker-compose down

echo "✅ Database stopped / Base de datos detenida"

