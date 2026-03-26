#!/bin/bash
# =========================================================
# Switch Environment Script
# Script para Cambiar de Ambiente
# =========================================================
# This script switches between different environment configurations
# by copying the selected env file to the project root as .env
#
# Este script cambia entre diferentes configuraciones de ambiente
# copiando el archivo seleccionado a la raíz del proyecto como .env
#
# Templates: envs/.env-{environment}   (en la raíz del proyecto)
# Active .env: .env                    (en la raíz del proyecto)
# =========================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Resolve directories
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"
ENVS_DIR="$PROJECT_ROOT/envs"
ENV_TARGET="$PROJECT_ROOT/.env"

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
    echo "Templates location / Ubicación de templates:"
    echo "  $ENVS_DIR/"
    echo ""
}

# Function to backup current .env if exists
backup_env() {
    if [ -f "$ENV_TARGET" ]; then
        TIMESTAMP=$(date +%Y%m%d_%H%M%S)
        BACKUP_FILE="$PROJECT_ROOT/.env.backup.$TIMESTAMP"
        cp "$ENV_TARGET" "$BACKUP_FILE"
        echo -e "${YELLOW}ℹ️  Backed up current .env to .env.backup.$TIMESTAMP${NC}"
    fi
}

# Function to display current environment
show_current_env() {
    if [ -f "$ENV_TARGET" ]; then
        CURRENT_ENV=$(grep "^KEYGO_ENV=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)
        if [ -n "$CURRENT_ENV" ]; then
            echo -e "${GREEN}Current environment / Ambiente actual: ${YELLOW}$CURRENT_ENV${NC}"
        else
            echo -e "${YELLOW}No environment detected in .env${NC}"
        fi
    else
        echo -e "${YELLOW}No .env file found in project root / No se encontró .env en la raíz${NC}"
    fi
}

# Function to switch environment
switch_environment() {
    local ENV=$1
    local TEMPLATE_FILE="$ENVS_DIR/.env-$ENV"

    # Check if template file exists
    if [ ! -f "$TEMPLATE_FILE" ]; then
        echo -e "${RED}❌ Error: Template file not found / Archivo de plantilla no encontrado${NC}"
        echo -e "${RED}   Looking for: $TEMPLATE_FILE${NC}"
        echo ""
        echo "Please create the file first using .env.example as template:"
        echo "  cp $ENVS_DIR/.env.example $ENVS_DIR/.env-$ENV"
        echo ""
        exit 1
    fi

    # Backup current .env
    backup_env

    # Copy environment template to project root .env
    cp "$TEMPLATE_FILE" "$ENV_TARGET"

    echo -e "${GREEN}✅ Successfully switched to: ${YELLOW}$ENV${NC}"
    echo -e "   Active .env: ${BLUE}$ENV_TARGET${NC}"
    echo ""

    # Display configuration summary
    echo -e "${BLUE}Configuration Summary / Resumen de Configuración:${NC}"
    echo "─────────────────────────────────────────────────"

    # Read and display key values (non-sensitive only)
    if [ -f "$ENV_TARGET" ]; then
        ENV_NAME=$(grep "^KEYGO_ENV=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)
        PROFILES=$(grep "^SPRING_PROFILES_ACTIVE=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)
        DB_HOST=$(grep "^SUPABASE_DB_HOST=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)
        DB_PORT=$(grep "^SUPABASE_DB_PORT=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)
        DB_NAME=$(grep "^SUPABASE_DB_NAME=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)
        SERVER_PORT=$(grep "^SERVER_PORT=" "$ENV_TARGET" 2>/dev/null | cut -d'=' -f2)

        echo -e "  Environment:      ${YELLOW}$ENV_NAME${NC}"
        echo -e "  Spring Profiles:  ${YELLOW}$PROFILES${NC}"
        echo -e "  Server Port:      ${YELLOW}${SERVER_PORT:-8080}${NC}"
        [ -n "$DB_HOST" ] && echo -e "  Database Host:    ${YELLOW}$DB_HOST${NC}"
        [ -n "$DB_PORT" ] && echo -e "  Database Port:    ${YELLOW}$DB_PORT${NC}"
        [ -n "$DB_NAME" ] && echo -e "  Database Name:    ${YELLOW}$DB_NAME${NC}"
    fi

    echo ""
    echo -e "${GREEN}🔄 Next steps / Próximos pasos:${NC}"
    echo "  1. Source the .env in your shell:"
    echo "     set -a; source .env; set +a"
    echo ""
    echo "  2. Restart your application or IDE to apply changes"
    echo ""

    if [ "$ENV" = "local" ]; then
        echo -e "${BLUE}💡 Local Development Tips:${NC}"
        echo "  • Menú principal:         ./scripts/keygo.sh"
        echo "  • Start local database:   ./scripts/keygo.sh 5  (o ./scripts/db/start.sh)"
        echo "  • Run migrations:         ./scripts/keygo.sh 7  (o ./scripts/db/migrate.sh)"
        echo "  • Quick-start (all-in-1): ./scripts/keygo.sh 12"
        echo "  • PgAdmin:                http://localhost:5050"
    elif [ "$ENV" = "desa" ]; then
        echo -e "${BLUE}💡 Development/Staging Tips:${NC}"
        echo "  • Menú principal:   ./scripts/keygo.sh"
        echo "  • Run migrations:   ./scripts/keygo.sh 7"
        echo "  • Make sure you have Supabase project credentials"
        echo "  • Test migrations carefully before production"
    elif [ "$ENV" = "prod" ]; then
        echo -e "${RED}⚠️  PRODUCTION WARNING:${NC}"
        echo "  • Menú principal:   ./scripts/keygo.sh"
        echo "  • Be extremely careful with database operations"
        echo "  • Always backup before running migrations"
        echo "  • Test in desa environment first"
        echo "  • Never commit .env to git!"
    fi
    echo ""
}

# Function to list all available environment templates
list_environments() {
    echo -e "${BLUE}Available Environment Templates / Plantillas de Ambiente Disponibles:${NC}"
    echo "  Location: $ENVS_DIR"
    echo "────────────────────────────────────────────────────────"

    local found=false
    for env_file in "$ENVS_DIR"/.env-*; do
        if [ -f "$env_file" ]; then
            found=true
            ENV_NAME=$(basename "$env_file" | sed 's/\.env-//')
            ENV_VAR=$(grep "^KEYGO_ENV=" "$env_file" 2>/dev/null | cut -d'=' -f2)
            PROFILES=$(grep "^SPRING_PROFILES_ACTIVE=" "$env_file" 2>/dev/null | cut -d'=' -f2)
            echo -e "  • ${GREEN}$ENV_NAME${NC}"
            [ -n "$ENV_VAR" ]  && echo -e "    KEYGO_ENV=$ENV_VAR"
            [ -n "$PROFILES" ] && echo -e "    SPRING_PROFILES_ACTIVE=$PROFILES"
        fi
    done

    if [ "$found" = false ]; then
        echo -e "  ${YELLOW}No templates found in $ENVS_DIR${NC}"
        echo "  Create one from the example:"
        echo "    cp $ENVS_DIR/.env.example $ENVS_DIR/.env-local"
    fi
    echo ""
}

# ─── Main ──────────────────────────────────────────────────────────────────────

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


