#!/bin/bash
# =========================================================
# db/setup.sh — Setup completo de Supabase para KeyGo
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

load_env || true
verify_db_vars || exit 1

echo "🚀 KeyGo Supabase Setup"
echo "════════════════════════"
echo "   Ambiente: ${KEYGO_ENV:-desconocido}"
echo ""

echo "📦 Ejecutando migraciones de base de datos..."
cd "$SUPABASE_DIR"
mvn flyway:migrate \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"

echo ""
echo "✅ Setup completado"
echo ""
echo "🎉 Credenciales de seed (solo dev/local):"
echo "   keygo_admin    / admin@keygo.local   → Admin1234!"
echo "   demo_admin     / admin@demo.local     → DevAdmin1!"
echo ""
echo "⚠️  IMPORTANTE: Cambiar contraseñas antes de usar en producción."

