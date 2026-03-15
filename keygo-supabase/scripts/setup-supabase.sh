#!/bin/bash
# =========================================================
# Setup Supabase for KeyGo
# Configurar Supabase para KeyGo
# =========================================================
set -e
echo "🚀 KeyGo Supabase Setup / Configuración de Supabase para KeyGo"
echo "=============================================================="
# Check if required environment variables are set
if [ -z "$SUPABASE_URL" ] || [ -z "$SUPABASE_KEY" ]; then
    echo "❌ Error: SUPABASE_URL and SUPABASE_KEY must be set"
    echo "❌ Error: SUPABASE_URL y SUPABASE_KEY deben estar configuradas"
    echo ""
    echo "Example / Ejemplo:"
    echo "  export SUPABASE_URL=postgresql://postgres:[PASSWORD]@db.[PROJECT].supabase.co:5432/postgres"
    echo "  export SUPABASE_KEY=your-anon-key"
    exit 1
fi
echo "✅ Environment variables configured / Variables de entorno configuradas"
echo ""
# Run Flyway migration
echo "📦 Running database migrations / Ejecutando migraciones de base de datos..."
mvn flyway:migrate \
    -Dflyway.url="$SUPABASE_URL" \
    -Dflyway.user=postgres \
    -Dflyway.password="$SUPABASE_PASSWORD"
echo ""
echo "✅ Migrations completed successfully / Migraciones completadas exitosamente"
echo ""
echo "🎉 Setup complete! / ¡Configuración completa!"
echo ""
echo "Default admin credentials / Credenciales de administrador por defecto:"
echo "  Username / Usuario: admin"
echo "  Password / Contraseña: admin123"
echo ""
echo "⚠️  IMPORTANT: Change the admin password after first login!"
echo "⚠️  IMPORTANTE: ¡Cambia la contraseña del administrador después del primer login!"
