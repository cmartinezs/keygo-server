#!/bin/bash
# =========================================================
# Load Environment Variables Helper
# Helper para Cargar Variables de Entorno
# =========================================================
# This script is sourced by other scripts to load .env variables
# Este script es usado por otros scripts para cargar variables .env
# =========================================================

# Get the project directory
if [ -n "$PROJECT_DIR" ]; then
    ENV_FILE="$PROJECT_DIR/.env"
else
    SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
    ENV_FILE="$PROJECT_DIR/.env"
fi

# Function to load environment from .env file
load_env() {
    if [ -f "$ENV_FILE" ]; then
        echo "✅ Loading environment from: $ENV_FILE"

        # Export variables from .env file
        set -a
        source <(cat "$ENV_FILE" | grep -v '^#' | grep -v '^$')
        set +a

        # Display current environment
        if [ -n "$KEYGO_ENV" ]; then
            echo "   Environment: $KEYGO_ENV"
        fi

        if [ -n "$SUPABASE_DB_HOST" ]; then
            echo "   Database: ${SUPABASE_DB_HOST}:${SUPABASE_DB_PORT:-5432}/${SUPABASE_DB_NAME:-postgres}"
        fi

        return 0
    else
        echo "⚠️  Warning: .env file not found at: $ENV_FILE"
        echo "   Using system environment variables (if set)"
        echo ""
        echo "💡 To create .env file, run (from project root):"
        echo "   ./scripts/switch-env.sh local   # or desa, prod"
        echo "   Templates are in: scripts/envs/"
        echo ""
        return 1
    fi
}

# Function to verify required variables are set
verify_required_vars() {
    local missing_vars=()

    if [ -z "$SUPABASE_URL" ]; then
        missing_vars+=("SUPABASE_URL")
    fi

    if [ -z "$SUPABASE_USER" ]; then
        missing_vars+=("SUPABASE_USER")
    fi

    if [ -z "$SUPABASE_PASSWORD" ]; then
        missing_vars+=("SUPABASE_PASSWORD")
    fi

    if [ ${#missing_vars[@]} -gt 0 ]; then
        echo "❌ Error: Required environment variables not set:"
        for var in "${missing_vars[@]}"; do
            echo "   - $var"
        done
        echo ""
        echo "Please ensure these variables are defined in .env file"
        echo "Run: ./scripts/switch-env.sh [local|desa|prod]"
        return 1
    fi

    return 0
}

# Function to display current environment info
show_env_info() {
    echo ""
    echo "Current Environment Configuration:"
    echo "─────────────────────────────────────"
    echo "  Environment:  ${KEYGO_ENV:-not set}"
    echo "  Profiles:     ${SPRING_PROFILES_ACTIVE:-not set}"
    echo "  DB Host:      ${SUPABASE_DB_HOST:-not set}"
    echo "  DB Port:      ${SUPABASE_DB_PORT:-not set}"
    echo "  DB Name:      ${SUPABASE_DB_NAME:-not set}"
    echo "  DB User:      ${SUPABASE_USER:-not set}"
    echo "  Project ID:   ${SUPABASE_PROJECT_ID:-not set}"
    echo "─────────────────────────────────────"
    echo ""
}

# Auto-load environment when script is sourced
# Only if not already loaded
if [ -z "$ENV_LOADED" ]; then
    load_env
    export ENV_LOADED=true
fi

