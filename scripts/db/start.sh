#!/bin/bash
# =========================================================
# db/start.sh — Iniciar base de datos local (Docker Compose)
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

echo "🚀 Iniciando base de datos local (Docker Compose)..."
cd "$SUPABASE_DIR"
docker-compose up -d

echo ""
echo "✅ Base de datos iniciada"
echo ""
echo "📊 Conexión:"
echo "   Host:     localhost:5432"
echo "   DB:       keygo"
echo "   Usuario:  postgres / Contraseña: postgres"
echo ""
echo "🔧 PgAdmin → http://localhost:5050"
echo "   Email:    admin@keygo.local / Contraseña: admin"

