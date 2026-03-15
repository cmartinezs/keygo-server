#!/bin/bash
# =========================================================
# Switch Environment Script
# Script para Cambiar de Ambiente
# =========================================================
# This script switches between different environment configurations
# by copying the selected .env-{environment} file to .env
#
# Este script cambia entre diferentes configuraciones de ambiente
# copiando el archivo .env-{ambiente} seleccionado a .env
# =========================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   KeyGo Environment Switcher               ║${NC}"
echo -e "${BLUE}║   Cambiador de Ambiente KeyGo              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# Function to display usage
show_usage() {
    echo "Usage / Uso:"
    echo "  $0 [environment]"
    echo ""
    echo "Available environments / Ambientes disponibles:"
    echo "  local - Local development with Docker Compose"
    echo "  desa  - Development/Staging Supabase instance"
    echo "  prod  - Production Supabase instance"
    echo ""
    echo "Example / Ejemplo:"
    echo "  $0 local"
    echo "  $0 desa"
    echo ""
}

# Function to backup current .env if exists
backup_env() {
    if [ -f "$PROJECT_DIR/.env" ]; then
        TIMESTAMP=$(date +%Y%m%d_%H%M%S)
        BACKUP_FILE="$PROJECT_DIR/.env.backup.$TIMESTAMP"
        cp "$PROJECT_DIR/.env" "$BACKUP_FILE"
        echo -e "${YELLOW}ℹ️  Backed up current .env to .env.backup.$TIMESTAMP${NC}"
    fi
}

# Function to display current environment
show_current_env() {
    if [ -f "$PROJECT_DIR/.env" ]; then
        CURRENT_ENV=$(grep "^KEYGO_ENV=" "$PROJECT_DIR/.env" | cut -d'=' -f2)
        if [ -n "$CURRENT_ENV" ]; then
            echo -e "${GREEN}Current environment / Ambiente actual: ${YELLOW}$CURRENT_ENV${NC}"
        else
            echo -e "${YELLOW}No environment detected / No se detectó ambiente${NC}"
        fi
    else
        echo -e "${YELLOW}No .env file found / No se encontró archivo .env${NC}"
    fi
}

# Function to switch environment
switch_environment() {
    local ENV=$1
    local ENV_FILE="$PROJECT_DIR/.env-$ENV"

    # Check if environment file exists
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}❌ Error: Environment file not found / Archivo de ambiente no encontrado${NC}"
        echo -e "${RED}   Looking for: $ENV_FILE${NC}"
        echo ""
        echo "Please create the file first using .env.example as template:"
        echo "  cp $PROJECT_DIR/.env.example $ENV_FILE"
        echo ""
        exit 1
    fi

    # Backup current .env
    backup_env

    # Copy environment file to .env
    cp "$ENV_FILE" "$PROJECT_DIR/.env"

    echo -e "${GREEN}✅ Successfully switched to: ${YELLOW}$ENV${NC}"
    echo ""

    # Display configuration summary
    echo -e "${BLUE}Configuration Summary / Resumen de Configuración:${NC}"
    echo "─────────────────────────────────────────────────"

    # Read and display key values
    if [ -f "$PROJECT_DIR/.env" ]; then
        ENV_NAME=$(grep "^KEYGO_ENV=" "$PROJECT_DIR/.env" | cut -d'=' -f2)
        PROFILES=$(grep "^SPRING_PROFILES_ACTIVE=" "$PROJECT_DIR/.env" | cut -d'=' -f2)
        DB_HOST=$(grep "^SUPABASE_DB_HOST=" "$PROJECT_DIR/.env" | cut -d'=' -f2)
        DB_PORT=$(grep "^SUPABASE_DB_PORT=" "$PROJECT_DIR/.env" | cut -d'=' -f2)
        DB_NAME=$(grep "^SUPABASE_DB_NAME=" "$PROJECT_DIR/.env" | cut -d'=' -f2)
        API_URL=$(grep "^SUPABASE_API_URL=" "$PROJECT_DIR/.env" | cut -d'=' -f2)

        echo -e "  Environment:      ${YELLOW}$ENV_NAME${NC}"
        echo -e "  Spring Profiles:  ${YELLOW}$PROFILES${NC}"
        echo -e "  Database Host:    ${YELLOW}$DB_HOST${NC}"
        echo -e "  Database Port:    ${YELLOW}$DB_PORT${NC}"
        echo -e "  Database Name:    ${YELLOW}$DB_NAME${NC}"
        echo -e "  API URL:          ${YELLOW}$API_URL${NC}"
    fi

    echo ""
    echo -e "${GREEN}🔄 Next steps / Próximos pasos:${NC}"
    echo "  1. Your IDE will load variables from .env file"
    echo "     Tu IDE cargará las variables desde el archivo .env"
    echo ""
    echo "  2. Restart your application or IDE to apply changes"
    echo "     Reinicia tu aplicación o IDE para aplicar los cambios"
    echo ""

    if [ "$ENV" = "local" ]; then
        echo -e "${BLUE}💡 Local Development Tips:${NC}"
        echo "  • Start local database: ./scripts/dev-start.sh"
        echo "  • Run migrations: ./scripts/migrate.sh"
        echo "  • PgAdmin: http://localhost:5050"
    elif [ "$ENV" = "desa" ]; then
        echo -e "${BLUE}💡 Development/Staging Tips:${NC}"
        echo "  • Make sure you have Supabase project credentials"
        echo "  • Test migrations carefully before production"
        echo "  • Check Supabase dashboard for database status"
    elif [ "$ENV" = "prod" ]; then
        echo -e "${RED}⚠️  PRODUCTION WARNING:${NC}"
        echo "  • Be extremely careful with database operations"
        echo "  • Always backup before running migrations"
        echo "  • Test in desa environment first"
        echo "  • Never commit .env to git!"
    fi
    echo ""
}

# Function to list all environments
list_environments() {
    echo -e "${BLUE}Available Environment Files:${NC}"
    echo "────────────────────────────────"

    for env_file in "$PROJECT_DIR"/.env-*; do
        if [ -f "$env_file" ]; then
            ENV_NAME=$(basename "$env_file" | sed 's/\.env-//')
            ENV_VAR=$(grep "^KEYGO_ENV=" "$env_file" | cut -d'=' -f2)
            echo -e "  • ${GREEN}$ENV_NAME${NC} ($env_file)"
            echo -e "    KEYGO_ENV=$ENV_VAR"
        fi
    done
    echo ""
}

# Main script logic
cd "$PROJECT_DIR"

# Show current environment
show_current_env
echo ""

# Check if environment parameter is provided
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}No environment specified / No se especificó ambiente${NC}"
    echo ""
    list_environments
    show_usage
    exit 1
fi

ENV_PARAM=$1

# Validate environment parameter
case $ENV_PARAM in
    local|desa|prod)
        switch_environment "$ENV_PARAM"
        ;;
    list)
        list_environments
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo -e "${RED}❌ Invalid environment: $ENV_PARAM${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac

