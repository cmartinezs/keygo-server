#!/bin/bash
# =========================================================
# _load-env.sh — Internal helper (source this, don't run it)
# Helper interno — usar con "source", no ejecutar directamente
# =========================================================
# Usage: source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"
# Provides: PROJECT_ROOT, ENV_FILE, load_env(), verify_db_vars()
# =========================================================

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"
ENV_FILE="$PROJECT_ROOT/.env"

# Load active .env from project root
load_env() {
    if [ -f "$ENV_FILE" ]; then
        set -a
        # shellcheck disable=SC1090
        source <(grep -v '^#' "$ENV_FILE" | grep -v '^$')
        set +a
        local env_name
        env_name=$(grep "^KEYGO_ENV=" "$ENV_FILE" 2>/dev/null | cut -d'=' -f2)
        echo "✅ Ambiente cargado: ${env_name:-desconocido}  ($ENV_FILE)"
        return 0
    else
        echo "⚠️  No se encontró .env en la raíz del proyecto ($PROJECT_ROOT/.env)"
        echo "   Ejecuta: ./scripts/switch-env.sh <env>"
        return 1
    fi
}

# Verify DB variables are present
verify_db_vars() {
    local missing=()
    [ -z "$SUPABASE_URL" ]      && missing+=("SUPABASE_URL")
    [ -z "$SUPABASE_USER" ]     && missing+=("SUPABASE_USER")
    [ -z "$SUPABASE_PASSWORD" ] && missing+=("SUPABASE_PASSWORD")
    if [ ${#missing[@]} -gt 0 ]; then
        echo "❌ Faltan variables de entorno:"
        for v in "${missing[@]}"; do echo "   • $v"; done
        echo ""
        echo "   Ejecuta: ./scripts/switch-env.sh <env>"
        return 1
    fi
    return 0
}

