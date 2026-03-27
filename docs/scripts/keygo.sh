#!/bin/bash
# =========================================================
# keygo.sh — CLI principal de KeyGo Server
# =========================================================
# Punto de entrada centralizado para todas las operaciones
# del proyecto: ambiente, base de datos, app, tests y setup.
#
# Uso:
#   ./docs/scripts/keygo.sh          # menú interactivo
#   ./docs/scripts/keygo.sh <opción> # ejecución directa (ej: ./docs/scripts/keygo.sh 7)
# =========================================================

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

# ── Colores ────────────────────────────────────────────────────────────────────
RED='\033[0;31m';   GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m';  CYAN='\033[0;36m';  BOLD='\033[1m';  NC='\033[0m'

# ── Funciones de apoyo ─────────────────────────────────────────────────────────
_current_env() {
    local env_file="$PROJECT_ROOT/.env"
    if [ -f "$env_file" ]; then
        grep "^KEYGO_ENV=" "$env_file" 2>/dev/null | cut -d'=' -f2
    else
        echo "sin-configurar"
    fi
}

_current_profiles() {
    local env_file="$PROJECT_ROOT/.env"
    [ -f "$env_file" ] && grep "^SPRING_PROFILES_ACTIVE=" "$env_file" 2>/dev/null | cut -d'=' -f2 || echo "—"
}

_current_port() {
    local env_file="$PROJECT_ROOT/.env"
    [ -f "$env_file" ] && grep "^SERVER_PORT=" "$env_file" 2>/dev/null | cut -d'=' -f2 || echo "8080"
}

_run() { bash "$SCRIPT_DIR/$1" "${@:2}"; }

_pause() {
    echo ""
    read -r -p "  Presiona ENTER para continuar..." _
}

# ── Banner ─────────────────────────────────────────────────────────────────────
_banner() {
    clear
    local ENV_NAME; ENV_NAME="$(_current_env)"
    local PROFILES; PROFILES="$(_current_profiles)"
    local PORT; PORT="$(_current_port)"

    # Color del ambiente
    local ENV_COLOR="$GREEN"
    [[ "$ENV_NAME" == "prod" ]]          && ENV_COLOR="$RED"
    [[ "$ENV_NAME" == "desa" ]]          && ENV_COLOR="$YELLOW"
    [[ "$ENV_NAME" == "sin-configurar" ]] && ENV_COLOR="$RED"

    echo -e "${BOLD}${BLUE}"
    echo "  ╔══════════════════════════════════════════════════╗"
    echo "  ║          🔑  KeyGo Server CLI                   ║"
    echo "  ║          Gestión centralizada del proyecto       ║"
    echo "  ╚══════════════════════════════════════════════════╝"
    echo -e "${NC}"
    printf "  Ambiente: ${ENV_COLOR}${BOLD}%-10s${NC}" "$ENV_NAME"
    printf "  Perfiles: ${CYAN}%-22s${NC}" "$PROFILES"
    printf "  Puerto: ${CYAN}%s${NC}\n" "$PORT"
    echo -e "  ${BLUE}────────────────────────────────────────────────────${NC}"
    echo ""
}

# ── Menú ───────────────────────────────────────────────────────────────────────
_menu() {
    echo -e "  ${BOLD}🌐  AMBIENTE${NC}"
    echo -e "  ${CYAN} 1)${NC} Cambiar a local     ${CYAN} 2)${NC} Cambiar a desa     ${CYAN} 3)${NC} Cambiar a prod"
    echo -e "  ${CYAN} 4)${NC} Listar ambientes"
    echo ""
    echo -e "  ${BOLD}🗄️   BASE DE DATOS (LOCAL Docker)${NC}"
    echo -e "  ${CYAN} 5)${NC} Iniciar DB          ${CYAN} 6)${NC} Detener DB"
    echo -e "  ${CYAN} 7)${NC} Ejecutar migraciones ${CYAN} 8)${NC} Ver estado (info)"
    echo -e "  ${CYAN} 9)${NC} Validar             ${CYAN}10)${NC} Reparar metadatos"
    echo -e "  ${RED}11)${NC} ⚠️  Limpiar schema (DESTRUCTIVO)"
    echo ""
    echo -e "  ${BOLD}🚀  APLICACIÓN${NC}"
    echo -e "  ${CYAN}12)${NC} Quick Start (DB + App)   ${CYAN}13)${NC} Build del proyecto"
    echo -e "  ${CYAN}14)${NC} Correr servidor local"
    echo ""
    echo -e "  ${BOLD}🧪  TESTS & VERIFICACIÓN${NC}"
    echo -e "  ${CYAN}15)${NC} Smoke test: service/info ${CYAN}16)${NC} Smoke test: response-codes"
    echo -e "  ${CYAN}17)${NC} Verificar docs AI        ${CYAN}18)${NC} Tests unitarios (Maven)"
    echo ""
    echo -e "  ${BOLD}⚙️   SETUP & CONFIGURACIÓN${NC}"
    echo -e "  ${CYAN}19)${NC} Setup inicial de tenant  ${CYAN}20)${NC} Setup Supabase (DB remota)"
    echo ""
    echo -e "  ${BLUE}────────────────────────────────────────────────────${NC}"
    echo -e "  ${YELLOW} q)${NC} Salir / Exit"
    echo ""
    printf "  ${BOLD}Elige una opción: ${NC}"
}

# ── Dispatcher ────────────────────────────────────────────────────────────────
_execute() {
    local OPT="$1"
    echo ""
    case "$OPT" in
        # ── Ambiente ──────────────────────────────────────────────────────────
        1) _run switch-env.sh local ;;
        2) _run switch-env.sh desa  ;;
        3) _run switch-env.sh prod  ;;
        4) _run switch-env.sh list  ;;

        # ── Base de datos ─────────────────────────────────────────────────────
        5)  _run db/start.sh    ;;
        6)  _run db/stop.sh     ;;
        7)  _run db/migrate.sh  ;;
        8)  _run db/info.sh     ;;
        9)  _run db/validate.sh ;;
        10) _run db/repair.sh   ;;
        11) _run db/clean.sh    ;;

        # ── Aplicación ────────────────────────────────────────────────────────
        12) _run quick-start.sh ;;
        13)
            echo -e "${BLUE}🔨 Construyendo proyecto...${NC}"
            cd "$PROJECT_ROOT" && ./mvnw clean package
            ;;
        14)
            echo -e "${BLUE}🚀 Iniciando servidor...${NC}"
            echo -e "${YELLOW}   Carga el .env antes con: set -a; source keygo-supabase/.env; set +a${NC}"
            echo ""
            cd "$PROJECT_ROOT" && ./mvnw spring-boot:run -pl keygo-run
            ;;

        # ── Tests ─────────────────────────────────────────────────────────────
        15) _run test-service-info.sh    ;;
        16) _run test-response-codes.sh  ;;
        17) _run check-ai-docs.sh        ;;
        18)
            echo -e "${BLUE}🧪 Ejecutando tests unitarios...${NC}"
            cd "$PROJECT_ROOT" && ./mvnw test
            ;;

        # ── Setup ─────────────────────────────────────────────────────────────
        19) _run setup-keygo-tenant.sh ;;
        20) _run db/setup.sh           ;;

        # ── Control ───────────────────────────────────────────────────────────
        q|Q|exit|quit|salir) echo -e "${GREEN}👋 ¡Hasta luego!${NC}"; echo ""; exit 0 ;;
        *)
            echo -e "${RED}❌ Opción inválida: '$OPT'${NC}"
            echo "   Opciones válidas: 1–20, q"
            ;;
    esac
}

# ── Entry point ───────────────────────────────────────────────────────────────
cd "$PROJECT_ROOT"

# Modo directo: si se pasa argumento, ejecutar y salir
if [ $# -gt 0 ]; then
    _execute "$1"
    exit $?
fi

# Modo interactivo: mostrar menú en bucle
while true; do
    _banner
    _menu
    read -r OPT
    _execute "$OPT"
    _pause
done

