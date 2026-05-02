#!/bin/bash
# =========================================================
# docker-run.sh — Construye y lanza el contenedor KeyGo
# =========================================================
# Carga variables de entorno desde .env (raíz del proyecto),
# construye la imagen Docker y ejecuta el contenedor.
#
# Uso:
#   ./scripts/docker-run.sh [opciones]
#
# Opciones:
#   --build         Forzar reconstrucción de la imagen antes de iniciar
#   --detach, -d    Ejecutar el contenedor en segundo plano (daemon)
#   --name NAME     Nombre del contenedor (default: keygo-server)
#   --port PORT     Puerto host a exponer (default: el de .env o 8080)
#   --env-file FILE Ruta alternativa al archivo .env
#   --help, -h      Mostrar esta ayuda
#
# Variables requeridas en .env:
#   SUPABASE_URL, SUPABASE_USER, SUPABASE_PASSWORD
#
# Variables opcionales en .env (con defaults):
#   SUPABASE_DB_SCHEMA (default: public)
#   SPRING_PROFILES_ACTIVE (default: supabase)
#   SERVER_PORT (default: 8080)
#   KEYGO_ADMIN_KEY
#   KEYGO_ISSUER_BASE_URL
#   SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD
#   KEYGO_MAIL_FROM, KEYGO_MAIL_APP_NAME
#   LOG_LEVEL, LOG_LEVEL_KEYGO
# =========================================================

set -euo pipefail

# ── Directorios ────────────────────────────────────────────────────────────────
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# ── Colores ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Defaults ───────────────────────────────────────────────────────────────────
IMAGE_NAME="keygo-server"
IMAGE_TAG="latest"
CONTAINER_NAME="keygo-server"
FORCE_BUILD=false
DETACH=false
ENV_FILE="$PROJECT_ROOT/.env"
HOST_PORT=""          # se resolverá desde .env o default 8080
CONTAINER_PORT=8080

# ── Ayuda ──────────────────────────────────────────────────────────────────────
_usage() {
    grep "^#" "$0" | grep -v "^#!/" | sed 's/^# \{0,1\}//'
    exit 0
}

# ── Parser de argumentos ───────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
    case "$1" in
        --build)        FORCE_BUILD=true;     shift ;;
        --detach|-d)    DETACH=true;          shift ;;
        --name)         CONTAINER_NAME="$2";  shift 2 ;;
        --port)         HOST_PORT="$2";       shift 2 ;;
        --env-file)     ENV_FILE="$2";        shift 2 ;;
        --help|-h)      _usage ;;
        *)
            echo -e "${RED}❌ Opción desconocida: $1${NC}"
            echo "   Usa --help para ver las opciones disponibles."
            exit 1
            ;;
    esac
done

# ── Banner ─────────────────────────────────────────────────────────────────────
echo -e "${BOLD}${BLUE}"
echo "  ██╗  ██╗███████╗██╗   ██╗ ██████╗  ██████╗ "
echo "  ██║ ██╔╝██╔════╝╚██╗ ██╔╝██╔════╝ ██╔═══██╗"
echo "  █████╔╝ █████╗   ╚████╔╝ ██║  ███╗██║   ██║"
echo "  ██╔═██╗ ██╔══╝    ╚██╔╝  ██║   ██║██║   ██║"
echo "  ██║  ██╗███████╗   ██║   ╚██████╔╝╚██████╔╝"
echo "  ╚═╝  ╚═╝╚══════╝   ╚═╝    ╚═════╝  ╚═════╝ "
echo -e "${NC}"
echo -e "${BOLD}  🐳 Docker Run — KeyGo Server${NC}"
echo "  ──────────────────────────────────────────"
echo ""

# ── Verificar .env ─────────────────────────────────────────────────────────────
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}❌ Archivo .env no encontrado: $ENV_FILE${NC}"
    echo ""
    echo "   Opciones:"
    echo "   1. Ejecuta: ./scripts/switch-env.sh local"
    echo "   2. Copia el template: cp envs/.env.example .env  y edita los valores"
    echo "   3. Usa --env-file /ruta/a/tu.env"
    exit 1
fi

echo -e "  ${GREEN}✅ Cargando variables desde:${NC} $ENV_FILE"

# Cargar .env sin exportar al shell (solo para leerlas aquí)
# Docker recibirá el archivo directamente via --env-file
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

# ── Resolver puerto ────────────────────────────────────────────────────────────
if [ -z "$HOST_PORT" ]; then
    HOST_PORT="${SERVER_PORT:-8080}"
fi

# ── Validar variables requeridas ───────────────────────────────────────────────
MISSING=()
[ -z "${SUPABASE_URL:-}"      ] && MISSING+=("SUPABASE_URL")
[ -z "${SUPABASE_USER:-}"     ] && MISSING+=("SUPABASE_USER")
[ -z "${SUPABASE_PASSWORD:-}" ] && MISSING+=("SUPABASE_PASSWORD")

if [ ${#MISSING[@]} -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ Variables requeridas no definidas en $ENV_FILE:${NC}"
    for v in "${MISSING[@]}"; do
        echo -e "   ${YELLOW}• $v${NC}"
    done
    echo ""
    echo "   Edita $ENV_FILE y define los valores antes de continuar."
    exit 1
fi

# ── Resumen de configuración ───────────────────────────────────────────────────
ACTIVE_ENV="${KEYGO_ENV:-desconocido}"
ACTIVE_PROFILES="${SPRING_PROFILES_ACTIVE:-supabase}"
ACTIVE_SCHEMA="${SUPABASE_DB_SCHEMA:-public}"

echo ""
echo -e "  ${CYAN}Configuración de ejecución:${NC}"
echo "  ┌─────────────────────────────────────────────────────"
echo "  │  Ambiente       : ${ACTIVE_ENV}"
echo "  │  Perfiles       : ${ACTIVE_PROFILES}"
echo "  │  Imagen         : ${IMAGE_NAME}:${IMAGE_TAG}"
echo "  │  Contenedor     : ${CONTAINER_NAME}"
echo "  │  Puerto         : ${HOST_PORT} → ${CONTAINER_PORT}"
echo "  │  SUPABASE_URL   : ${SUPABASE_URL}"
echo "  │  SUPABASE_USER  : ${SUPABASE_USER}"
echo "  │  SUPABASE_PASSWORD : ****"
echo "  │  SUPABASE_DB_SCHEMA: ${ACTIVE_SCHEMA}"
echo "  └─────────────────────────────────────────────────────"
echo ""

# ── Detener contenedor previo si existe ────────────────────────────────────────
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$" 2>/dev/null; then
    echo -e "  ${YELLOW}⚠️  Contenedor '${CONTAINER_NAME}' ya existe — deteniéndolo...${NC}"
    docker rm -f "${CONTAINER_NAME}" > /dev/null 2>&1
    echo -e "  ${GREEN}✅ Contenedor anterior eliminado${NC}"
    echo ""
fi

# ── Build de imagen ────────────────────────────────────────────────────────────
_image_exists() {
    docker image inspect "${IMAGE_NAME}:${IMAGE_TAG}" > /dev/null 2>&1
}

if $FORCE_BUILD || ! _image_exists; then
    if $FORCE_BUILD; then
        echo -e "  ${CYAN}🔨 --build activado: reconstruyendo imagen...${NC}"
    else
        echo -e "  ${CYAN}🔨 Imagen no encontrada: construyendo...${NC}"
    fi
    echo ""
    docker build \
        --tag "${IMAGE_NAME}:${IMAGE_TAG}" \
        --file "$PROJECT_ROOT/Dockerfile" \
        "$PROJECT_ROOT"
    echo ""
    echo -e "  ${GREEN}✅ Imagen construida: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
else
    echo -e "  ${GREEN}✅ Imagen existente: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
    echo -e "     ${YELLOW}(usa --build para forzar reconstrucción)${NC}"
fi

echo ""

# ── Construir comando docker run ───────────────────────────────────────────────
DOCKER_RUN_ARGS=(
    run
    --name "${CONTAINER_NAME}"
    --publish "${HOST_PORT}:${CONTAINER_PORT}"
    --env-file "${ENV_FILE}"
    --restart unless-stopped
)

if $DETACH; then
    DOCKER_RUN_ARGS+=(--detach)
fi

DOCKER_RUN_ARGS+=("${IMAGE_NAME}:${IMAGE_TAG}")

# ── Lanzar contenedor ──────────────────────────────────────────────────────────
if $DETACH; then
    echo -e "  ${CYAN}🚀 Lanzando contenedor en segundo plano...${NC}"
    echo ""
    docker "${DOCKER_RUN_ARGS[@]}"
    echo ""
    echo -e "  ${GREEN}✅ Contenedor '${CONTAINER_NAME}' iniciado${NC}"
    echo ""
    echo -e "  ${BOLD}URLs disponibles (puede tardar ~40s en estar listo):${NC}"
    echo "  ┌─────────────────────────────────────────────────────────────────"
    echo "  │  Health     : http://localhost:${HOST_PORT}/keygo-server/actuator/health"
    echo "  │  Swagger UI : http://localhost:${HOST_PORT}/keygo-server/swagger-ui/index.html"
    echo "  │  OpenAPI    : http://localhost:${HOST_PORT}/keygo-server/v3/api-docs"
    echo "  │  Info       : http://localhost:${HOST_PORT}/keygo-server/api/v1/service/info"
    echo "  └─────────────────────────────────────────────────────────────────"
    echo ""
    echo -e "  ${BOLD}Comandos útiles:${NC}"
    echo "    docker logs -f ${CONTAINER_NAME}      # ver logs en tiempo real"
    echo "    docker stop ${CONTAINER_NAME}          # detener"
    echo "    docker rm -f ${CONTAINER_NAME}         # eliminar"
else
    echo -e "  ${CYAN}🚀 Lanzando contenedor (primer plano — Ctrl+C para detener)...${NC}"
    echo ""
    echo -e "  ${BOLD}URLs disponibles (puede tardar ~40s en estar listo):${NC}"
    echo "  ┌─────────────────────────────────────────────────────────────────"
    echo "  │  Health     : http://localhost:${HOST_PORT}/keygo-server/actuator/health"
    echo "  │  Swagger UI : http://localhost:${HOST_PORT}/keygo-server/swagger-ui/index.html"
    echo "  │  OpenAPI    : http://localhost:${HOST_PORT}/keygo-server/v3/api-docs"
    echo "  │  Info       : http://localhost:${HOST_PORT}/keygo-server/api/v1/service/info"
    echo "  └─────────────────────────────────────────────────────────────────"
    echo ""
    docker "${DOCKER_RUN_ARGS[@]}"
fi

