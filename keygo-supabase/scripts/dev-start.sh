#!/bin/bash
# =========================================================
# Start local development database
# Iniciar base de datos local de desarrollo
# =========================================================

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

echo "🚀 Starting local development database / Iniciando base de datos local de desarrollo..."

cd "$PROJECT_DIR"

# Start docker compose
docker-compose up -d

echo ""
echo "✅ Database started / Base de datos iniciada"
echo ""
echo "📊 Database connection info / Información de conexión:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: keygo"
echo "  User: postgres"
echo "  Password: postgres"
echo ""
echo "🔧 PgAdmin available at / PgAdmin disponible en: http://localhost:5050"
echo "  Email: admin@keygo.local"
echo "  Password: admin"
echo ""
echo "🔄 To run migrations, execute / Para ejecutar migraciones, ejecuta:"
echo "  export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo"
echo "  export SUPABASE_USER=postgres"
echo "  export SUPABASE_PASSWORD=postgres"
echo "  ./scripts/migrate.sh"

