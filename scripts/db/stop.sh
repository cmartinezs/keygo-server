#!/bin/bash
# =========================================================
# db/stop.sh — Detener base de datos local (Docker Compose)
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

echo "🛑 Deteniendo base de datos local..."
cd "$SUPABASE_DIR"
docker-compose down

echo "✅ Base de datos detenida"

