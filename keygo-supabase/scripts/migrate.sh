#!/bin/bash
# =========================================================
# Run Flyway migrations
# Ejecutar migraciones de Flyway
# =========================================================
set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
echo "📦 Running Flyway migrations / Ejecutando migraciones de Flyway..."
# Load environment variables from .env if exists
if [ -f "$PROJECT_DIR/.env" ]; then
    echo "✅ Loading environment from .env"
    export $(cat "$PROJECT_DIR/.env" | grep -v '^#' | grep -v '^$' | xargs)
else
    echo "⚠️  Warning: .env file not found. Using system environment variables."
    echo "   Run: ./scripts/switch-env.sh local (or desa/prod)"
fi
# Verify required variables
if [ -z "$SUPABASE_URL" ] || [ -z "$SUPABASE_USER" ] || [ -z "$SUPABASE_PASSWORD" ]; then
    echo "❌ Error: Required environment variables not set"
    echo "   SUPABASE_URL, SUPABASE_USER, SUPABASE_PASSWORD"
    echo ""
    echo "Please run: ./scripts/switch-env.sh [local|desa|prod]"
    exit 1
fi
echo "   Environment: ${KEYGO_ENV:-unknown}"
echo "   Database: ${SUPABASE_DB_HOST:-unknown}:${SUPABASE_DB_PORT:-5432}"
echo ""
cd "$PROJECT_DIR"
mvn clean compile flyway:migrate \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Migrations completed / Migraciones completadas"
