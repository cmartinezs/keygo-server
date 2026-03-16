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

