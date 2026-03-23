#!/bin/bash
# =========================================================
# Quick Start Script for KeyGo with Supabase
# Script de inicio rápido para KeyGo con Supabase
# =========================================================

set -e

echo "🚀 KeyGo + Supabase Quick Start"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Start local database
echo -e "${GREEN}Step 1: Starting local PostgreSQL database${NC}"
cd keygo-supabase
./scripts/dev-start.sh
cd ..

echo ""
echo -e "${YELLOW}Waiting for database to be ready...${NC}"
sleep 5

# Step 2: Set environment variables
echo -e "${GREEN}Step 2: Setting environment variables${NC}"
export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=postgres

echo "  ✅ SUPABASE_URL=$SUPABASE_URL"
echo "  ✅ SUPABASE_USER=$SUPABASE_USER"

# Optional: SMTP for email verification (registration flow)
# Opcional: SMTP para verificación de email (flujo de registro)
# Default uses MailHog on port 1025 (no auth needed)
# Default usa MailHog en puerto 1025 (sin autenticación)
export SMTP_HOST="${SMTP_HOST:-localhost}"
export SMTP_PORT="${SMTP_PORT:-1025}"
export SMTP_USERNAME="${SMTP_USERNAME:-}"
export SMTP_PASSWORD="${SMTP_PASSWORD:-}"
export KEYGO_MAIL_FROM="${KEYGO_MAIL_FROM:-noreply@keygo.local}"
export KEYGO_MAIL_APP_NAME="${KEYGO_MAIL_APP_NAME:-KeyGo}"

echo "  ✅ SMTP_HOST=$SMTP_HOST:$SMTP_PORT (override with env vars)"
echo ""

# Step 3: Run migrations
echo -e "${GREEN}Step 3: Running database migrations${NC}"
cd keygo-supabase
./scripts/migrate.sh
cd ..

echo ""

# Step 4: Build the application
echo -e "${GREEN}Step 4: Building the application${NC}"
./mvnw clean package -DskipTests

echo ""

# Step 5: Show migration info
echo -e "${GREEN}Step 5: Migration status${NC}"
cd keygo-supabase
./scripts/info.sh
cd ..

echo ""
echo "================================"
echo -e "${GREEN}🎉 Setup Complete!${NC}"
echo "================================"
echo ""
echo "📊 Database is running at:"
echo "   Host: localhost:5432"
echo "   Database: keygo"
echo "   User: postgres"
echo ""
echo "🔐 Default admin credentials:"
echo "   Username: admin"
echo "   Email: admin@keygo.local"
echo "   Password: admin123"
echo ""
echo "🌐 PgAdmin available at:"
echo "   http://localhost:5050"
echo "   Email: admin@keygo.local"
echo "   Password: admin"
echo ""
echo "📧 Email testing (MailHog — optional):"
echo "   docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog"
echo "   UI → http://localhost:8025"
echo "   SMTP_HOST=localhost SMTP_PORT=1025 (already set by this script)"
echo ""
echo "▶️  To start the application, run:"
echo "   ./mvnw spring-boot:run -pl keygo-run"
echo ""
echo "or"
echo ""
echo "   java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar"
echo ""
echo "🛑 To stop the database:"
echo "   cd keygo-supabase && ./scripts/dev-stop.sh"
echo ""

